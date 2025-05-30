package com.reactive.SportWatch.services;

import java.time.Duration;
import java.util.Optional;
import java.util.logging.Logger;

// import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
// import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.fasterxml.jackson.databind.MapperFeature;
// import com.amazonaws.services.lambda.runtime.events.S3Event.S3EventNotificationRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reactive.SportWatch.config.AwsCredentialsConfig;
import com.reactive.SportWatch.models.IvsChannelInfo;
import com.reactive.SportWatch.models.Streaming;
import com.reactive.SportWatch.models.S3EventJson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ivs.IvsClient;
import software.amazon.awssdk.services.ivs.model.Channel;
import software.amazon.awssdk.services.ivs.model.ChannelLatencyMode;
import software.amazon.awssdk.services.ivs.model.CreateChannelResponse;
import software.amazon.awssdk.services.ivs.model.GetStreamKeyRequest;
import software.amazon.awssdk.services.ivs.model.GetStreamRequest;
import software.amazon.awssdk.services.ivs.model.GetStreamResponse;
import software.amazon.awssdk.services.ivs.model.ListChannelsRequest;
import software.amazon.awssdk.services.ivs.model.StreamKey;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

@Service
public class UploadService {

    private final static Logger log = Logger.getLogger(UploadService.class.getName());

    private IvsClient ivs;
    private S3Client s3;
    private Bucket bucket;

    private SqsPoller sqsPoller;
    private StreamService streamService;

    private final StaticCredentialsProvider credentialsProvider;
    private final Region REGION = Region.EU_WEST_1;
    private final int MAXCHANNELS = 3;
    private final int POLLERINTERVAL = 30; // In secs, time it takes poller to check sqs queue and update streams.

    // "arn:aws:ivs:eu-west-1:173473165842:recording-configuration/Xz5nptwXD4Kf"
    // la conf 2 es la que tiene la config de sobreescritura de miniaturas
    private final String RECORDINGCONF = "arn:aws:ivs:eu-west-1:173473165842:recording-configuration/SjbJLZ7fGb8l";
    private final String SQSQUEUE = "arn:aws:sqs:eu-west-1:173473165842:stream-upload-queue";
    private final String QUEUEURL = "https://sqs.eu-west-1.amazonaws.com/173473165842/stream-upload-queue";
    private final String BUCKETNAME = "streams-ivs";

    // NOTE: Este servicio seguramente se quede como AwsUploadService, y se haga otro que devuleva directamente las direcciones a los m3u8 de los directos en curso y que cree url temporales
    // para los directos resubidos en el s3.

    @Autowired
    public UploadService(AwsCredentialsConfig awsCredentialsConfig, StreamService streamService) {
        log.info("UploadService Started...");
        this.credentialsProvider = StaticCredentialsProvider.create(awsCredentialsConfig.awsCredentials());
        this.ivs = IvsClient.builder()
            .region(REGION).credentialsProvider(credentialsProvider)
            .build();

        this.s3 = S3Client.builder()
            .region(REGION).credentialsProvider(credentialsProvider)
            .build();

        this.sqsPoller = new SqsPoller();
        this.streamService = streamService;

        Optional <Bucket> bucketOpt = s3.listBuckets().buckets().stream()
            .filter(b -> b.name().equals(BUCKETNAME))
            .findFirst();

        if (bucketOpt.isEmpty()) throw new IllegalArgumentException("Bucket: " + BUCKETNAME + " must exist");
        this.bucket = bucketOpt.get();

    }


    public Mono<IvsChannelInfo> newChannel(int user_id, String title, Optional<String> desc, Optional<String> category, Optional<ChannelLatencyMode> latency) {
        // Integer channelAmnt = ivs.listChannels(ListChannelsRequest.builder().build()).channels().size();
        Mono<Integer> channelAmnt = Mono.fromCallable(() -> ivs.listChannels(ListChannelsRequest.builder().build()).channels().size());

        return channelAmnt.flatMap((amnt) -> (amnt > MAXCHANNELS)
                ? Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "A new channel could not be created, limit of channels reached: " + channelAmnt))
                : Mono.just(amnt))
                .map(z -> {
                    CreateChannelResponse res = ivs.createChannel(createChannelBuilder -> createChannelBuilder
                                                                  .latencyMode(latency.orElse(ChannelLatencyMode.LOW))
                                                                  .recordingConfigurationArn(RECORDINGCONF)
                                                                  .build());

                    String rtmpsUrl = "rtmps:://" + res.channel().ingestEndpoint() + ":443/app/";
                    String arn = res.channel().arn();
                    streamService.createStream(new Streaming().authorId(user_id)
                                               .category(category.orElse("Desconocido")).arn(arn)
                                               .title(title).desc(desc.orElse("")));

                    var channelInfo = new IvsChannelInfo(res.streamKey().toString(), rtmpsUrl);
                    log.info("Channel info: " + channelInfo.toString());

                    return channelInfo;
                });
    }

    public Flux<Channel> listChannels() {
        return Mono.fromCallable(() -> ivs.listChannels(builder -> builder.build()).channels())
            .flatMapMany(Flux::fromIterable)
            .flatMap(summary -> Mono.fromCallable(() -> ivs.getChannel(req -> req.arn(summary.arn()).build()).channel()));

    }

    // if null channel info returned no free channel found.
    public Mono<IvsChannelInfo> findFreeChannel(int user_id, String title, Optional<String> desc, Optional<String> category) {
        return listChannels()
                .flatMap(channel -> Mono.fromCallable(() -> ivs.listStreamKeys(command -> command.channelArn(channel.arn()).build()).streamKeys())
                        .flatMapMany(Flux::fromIterable)
                        .next() // pick first stream key or empty if none
                        .flatMap(streamKeySummary -> isChannelLive(channel.arn())
                                .filter(isLive -> !isLive)
                                 // here goes the db logic, which is key
                                .map(notLive -> channel.arn())
                                .map(arn -> streamService.createStream(new Streaming().authorId(user_id)
                                        .category(category.orElse("Desconocido"))
                                        .arn(arn)
                                        .title(title)
                                        .desc(desc.orElse("")))
                                     )

                                .flatMap(streamingObj -> getStreamKey(streamKeySummary.arn())
                                        .map(key -> new IvsChannelInfo(key.value(), "rtmps:://" + channel.ingestEndpoint() + ":443/app/")))))
                .next() // take the first free channel found
                .defaultIfEmpty(new IvsChannelInfo(null, null));
    }


    private Mono<Boolean> isChannelLive(String channelArn) {
        return Mono.fromCallable(() -> {
            try {
                GetStreamResponse response = ivs.getStream(GetStreamRequest.builder().channelArn(channelArn).build());
                return response.stream() != null;
            } catch (Exception e) {
                // If no live stream, AWS SDK may throw an exception, so consider it not live
                return false;
            }
        });
    }

    private Mono<StreamKey> getStreamKey(String streamKeyArn) {
        return Mono.fromCallable(() -> ivs.getStreamKey(GetStreamKeyRequest.builder().arn(streamKeyArn).build()).streamKey());
    }


    protected class SqsPoller {

        private final static Logger log = Logger.getLogger(SqsPoller.class.getName());

        private static int test = 0;

        private final SqsAsyncClient sqs;
        private final String QUEUENAME = SQSQUEUE.substring(SQSQUEUE.lastIndexOf(":") + 1, SQSQUEUE.length());

        private final ObjectMapper objectMapper = new ObjectMapper().configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

        public SqsPoller() {
            this.sqs = SqsAsyncClient.builder().region(REGION).credentialsProvider(credentialsProvider).build();

            Flux.interval(Duration.ofSeconds(POLLERINTERVAL))
                .map(tick -> test++)
                .flatMap(tick -> this.pollStreamEvents())
                .subscribe();

        }

        // public SqsPoller setQueueUrl(String queueUrl) {
        //     this.queueUrl = queueUrl;
        //     return this;
        // }

        public Flux<String> pollStreamEvents() {
            log.info("Polling...");
            return Mono.fromFuture(() -> sqs.receiveMessage(
                        ReceiveMessageRequest.builder()
                            .queueUrl(QUEUEURL)
                            .maxNumberOfMessages(10)
                            .waitTimeSeconds(20)
                            .build()))
                    .flatMapMany(response -> Flux.fromIterable(response.messages()))
                    .flatMap(this::processMessage);
        }

        private Mono<String> processMessage(Message msg) {
            try {
                // log.info("SQS msg body: " + msg.body());
                // Parse the JSON
                S3EventJson notification = objectMapper.readValue(msg.body(), S3EventJson.class);

                // String bucketName = record.getS3().getBucket().getName();
                String objectKey = notification.Records.get(0).S3.object.key;

                // Ahora mismo solo proceso mensajes de mp4
                if (!objectKey.endsWith(".m3u8") && !objectKey.endsWith(".jpg"))
                    return Mono.fromFuture(() -> sqs.deleteMessage(DeleteMessageRequest.builder()
                                .queueUrl(QUEUEURL)
                                .receiptHandle(msg.receiptHandle())
                                .build()))
                            .thenReturn(objectKey);

                String channelId = getChannelIdFromObjectKey(objectKey);
                if (objectKey.endsWith("master.m3u8")) log.info("channelId del master: " + channelId);
                // Update the stream that 'wasnt uploaded' with its objectKey or if thumbnail was found with its object key.
                return streamService.findNonUploadedStreams().filter(streaming -> streaming.channelId().equals(channelId))
                    .next().flatMap(matchingStream -> {
                            if (objectKey.endsWith("master.m3u8")) {log.info("Uploading master.m3u8"); return streamService.updateStream(matchingStream.object_key(objectKey));}
                            if (objectKey.endsWith(".jpg")) return streamService.addThumbnailToStream(matchingStream.thumbnail_obj_key(objectKey));
                            log.warning("Object key wasn't either master or .jpg but a type:" + objectKey.split("\\.")[1]);
                            log.warning("Some invalid .m3u8 surely");
                            return Mono.empty();
                        })
                    .flatMap(dc -> Mono.fromFuture(() -> sqs.deleteMessage(DeleteMessageRequest.builder()
                                .queueUrl(QUEUEURL) // Make sure QUEUEURL is properly defined
                                .receiptHandle(msg.receiptHandle())
                                .build())))
                    .thenReturn(objectKey);

            } catch (Exception e) {
                return Mono.error(e);
            }
        }
        // Entre hacer parsing de esta manera y con .split, me gusta mas split la verdad, pero quería probar así porque se la veo a chatgpt. No me convence.
        // la channel id es lo que empieza por 64
        // Recorded stream	ivs/v1/173473165842/64TnBPjBxLH9/2025/5/29/18/48/DlB7WDw9nkVU/media/hls/720p30/playlist.m3u8
        // Thumbnail images ivs/v1/173473165842/64TnBPjBxLH9/2025/5/29/18/48/DlB7WDw9nkVU/media/thumbnails/thumb0.jpg
        // Este método sirve para el formato de una object_key de un .mp4,
        // la object_key de una miniatura se guarda en un sitio diferente.
        private String getChannelIdFromObjectKey(String objectKey) {
            return objectKey.split("/")[3];
        }


    }
}

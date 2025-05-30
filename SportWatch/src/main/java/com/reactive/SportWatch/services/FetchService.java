package com.reactive.SportWatch.services;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Optional;
import java.util.logging.Logger;

import com.reactive.SportWatch.config.AwsCredentialsConfig;
import com.reactive.SportWatch.models.StreamingInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ivs.IvsClient;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
public class FetchService {

    private final static Logger log = Logger.getLogger(FetchService.class.getName());

    private final StaticCredentialsProvider credentialsProvider;
    private final Region REGION = Region.EU_WEST_1;
    private final String BUCKETNAME = "streams-ivs";

    private StreamService streamService;
    private IvsClient ivs;
    private final S3Presigner presigner;

    @Autowired
    FetchService(AwsCredentialsConfig awsCredentialsConfig, StreamService streamService) {
        this.credentialsProvider = StaticCredentialsProvider.create(awsCredentialsConfig.awsCredentials());
        this.streamService = streamService;
        this.presigner = S3Presigner.builder()
            .region(REGION) // change to your region
            .credentialsProvider(credentialsProvider)
            .build();

        this.ivs = IvsClient.builder()
            .region(REGION).credentialsProvider(credentialsProvider)
            .build();
    }
     
    //  res.channel().playbackUrl(); el .m3u8

    // Create temporal links of its object keys for uploaded streams and return that and another temporal link for the thumbnail
    // IMPORTANT: you also need to return the Streaming object bc it has its title and author.
    // Thumbnail temporal link creation logic can be separated to a func bc im going to need it also for non uploadedStreams.
    public Flux<StreamingInfo> fetchUploadedStreams(Optional<Duration> urlDuration) {
        return streamService.findUploadedStreams()
            .flatMap(strm -> {
                    Mono<URL> streamUrlMono = getPresignedUrl(strm.object_key(), urlDuration);
                    Mono<URL> thumbnailUrlMono = getPresignedUrl(strm.thumbnail_obj_key(), urlDuration).defaultIfEmpty(null);

                    return Mono.zip(streamUrlMono, thumbnailUrlMono).map(tuple -> new StreamingInfo(strm.title(), strm.category(),
                                                                                              tuple.getT1(), tuple.getT2(), // Thumbnail URL
                                                                                              strm.authorId(), strm.desc(),
                                                                                              strm.created_at()));
                });
    }

    // for nonUploaded take its playbackUrl and also try to take the thumbnail.
    public Flux<StreamingInfo> fetchNonUploadedStreams(Optional<Duration> urlDuration) {
        return streamService.findNonUploadedStreams()
            .flatMap(strm -> {
                    try {
                        URL streamUrl = new URL(ivs.getChannel(req -> req.arn(strm.arn()).build()).channel().playbackUrl());
                        return getPresignedUrl(strm.thumbnail_obj_key(), urlDuration)
                                .defaultIfEmpty(null)
                                .map(thumbnailUrl -> new StreamingInfo(
                                        strm.title(), strm.category(),
                                        streamUrl, thumbnailUrl,
                                        strm.authorId(), strm.desc(),
                                        strm.created_at()
                                ));
                    } catch(MalformedURLException e) {
                        return Mono.error(new RuntimeException("Invalid playback URL: " + strm.arn(), e));
                    }
                })
                ;
    }

    // this one gets a stream by its stream id and depending on the object_key it generates presigned url or returns playback one.
    // and obv gets the playback
    // urlDuration may be not be used, depending if the stream is uploaded or currently live.
    // StreamService.findFileById now gets arn, bc i needed a way to select the channel in case the stream is live.
    public Mono<StreamingInfo> fetchStreamById(int streamId, Optional<Duration> urlDuration) {
        return streamService.findFileById(streamId)
            .flatMap(strm -> {
                    Mono<URL> streamUrlMono;
                    if (strm.object_key() != null)
                        streamUrlMono = getPresignedUrl(strm.object_key(), urlDuration);

                    else streamUrlMono = Mono.fromCallable(() -> {
                            try {
                                return new URL(ivs.getChannel(req -> req.arn(strm.arn()).build())
                                               .channel().playbackUrl());
                            } catch (MalformedURLException e) {
                                throw new RuntimeException("Invalid playback URL: " + strm.arn(), e);
                            }
                        });

                    if (strm.thumbnail_obj_key() != null) {
                        Mono<URL> thumbnailUrlMono = getPresignedUrl(strm.thumbnail_obj_key(), urlDuration);
                        return Mono.zip(streamUrlMono, thumbnailUrlMono).map(tuple -> new StreamingInfo(strm.title(), strm.category(),
                                                                                                        tuple.getT1(), tuple.getT2(), // Thumbnail URL
                                                                                                        strm.authorId(), strm.desc(),
                                                                                                        strm.created_at()));
                    }
                    else return streamUrlMono.map(streamUrl -> new StreamingInfo(strm.title(), strm.category(),
                                                                                 streamUrl, null, // Thumbnail URL
                                                                                 strm.authorId(), strm.desc(),
                                                                                 strm.created_at()));

                });
    }
    // Gets a temporary url for an object key of an mp4 or .jpg
    private Mono<URL> getPresignedUrl(String objectKey, Optional<Duration> duration) {
        if (objectKey == null) return Mono.empty();
        return Mono.fromCallable(() -> {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(BUCKETNAME)
                    .key(objectKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(duration.orElse(Duration.ofHours(2)))
                    .getObjectRequest(getObjectRequest)
                    .build();

            URL presignedUrl = presigner.presignGetObject(presignRequest).url();
            log.info("Presigned URL (valid for 2 hours): " + presignedUrl);
            return presignedUrl;
        });
    }

    @PreDestroy
    public void shutdown() {
        if (this.presigner != null) {
            presigner.close();
            log.info("S3Presigner closed");
        }
    }
   
}

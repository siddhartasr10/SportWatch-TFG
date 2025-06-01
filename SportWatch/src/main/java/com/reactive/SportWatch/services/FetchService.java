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

/**
 * Service responsible for fetching streaming data from AWS S3 and IVS.
 * It builds presigned URLs for video and thumbnail files, or retrieves live playback URLs.
 */
@Service
public class FetchService {

    private final static Logger log = Logger.getLogger(FetchService.class.getName());

    private final StaticCredentialsProvider credentialsProvider;
    private final Region REGION = Region.EU_WEST_1;
    private final String BUCKETNAME = "streams-ivs";

    private final StreamService streamService;
    private final UserService userService;

    private final IvsClient ivs;
    private final S3Presigner presigner;

    // Url switched if mono returns empty.
    // I do this here and in authController as mono cannot use null.
    private URL INVALIDURL;

    /**
     * Constructs a new {@link FetchService} using AWS credentials and stream service.
     *
     * @param awsCredentialsConfig the AWS credentials configuration
     * @param streamService the service used to query streaming metadata
     */
    @Autowired
    public FetchService(AwsCredentialsConfig awsCredentialsConfig, StreamService streamService, UserService userService) {
        this.credentialsProvider = StaticCredentialsProvider.create(awsCredentialsConfig.awsCredentials());
        this.streamService = streamService;
        this.userService = userService;

        this.presigner = S3Presigner.builder()
            .region(REGION)
            .credentialsProvider(credentialsProvider)
            .build();

        this.ivs = IvsClient.builder()
            .region(REGION)
            .credentialsProvider(credentialsProvider)
            .build();


        try {this.INVALIDURL = new URL("https://google.com");}
        catch (MalformedURLException e) {log.info(String.format("Error while initializing the invalid url: %s", e.getStackTrace()));}
    }

    /**
     * Fetches uploaded stream metadata and generates temporary URLs (presigned) for the video file and thumbnail.
     *
     * @param urlDuration optional custom duration for how long the URLs should remain valid
     * @return a Flux of {@link StreamingInfo} containing URLs and metadata
     */
    public Flux<StreamingInfo> fetchUploadedStreams(Optional<Duration> urlDuration) {
        return streamService.findUploadedStreams()
            .flatMap(strm -> {
                Mono<URL> streamUrlMono = getPresignedUrl(strm.object_key(), urlDuration);
                Mono<URL> thumbnailUrlMono = getPresignedUrl(strm.thumbnail_obj_key(), urlDuration);
                Mono<String> authorMono = userService.findUsernameById(strm.authorId());

                return Mono.zip(streamUrlMono, thumbnailUrlMono, authorMono)
                    .map(tuple -> new StreamingInfo(
                        strm.streamId(), strm.title(),
                        strm.category(), tuple.getT1(),
                        tuple.getT2(), tuple.getT3(),
                        strm.authorId(), strm.desc(),
                        strm.created_at()
                    ));
            });
    }

    /**
     * Fetches live (non-uploaded) streams using IVS playback URLs and presigned thumbnail links.
     *
     * @param urlDuration optional duration for thumbnail link validity
     * @return a Flux of {@link StreamingInfo} with playback URLs and metadata
     */
    public Flux<StreamingInfo> fetchNonUploadedStreams(Optional<Duration> urlDuration) {
        return streamService.findNonUploadedStreams()
            .flatMap(strm -> {
                try {
                    URL streamUrl = new URL(ivs.getChannel(req -> req.arn(strm.arn()).build()).channel().playbackUrl());
                    Mono<URL> thumbnailUrlMono = getPresignedUrl(strm.thumbnail_obj_key(), urlDuration);
                    Mono<String> authorMono = userService.findUsernameById(strm.authorId());

                    return Mono.zip(thumbnailUrlMono, authorMono)
                        .map(tuple -> new StreamingInfo(
                            strm.streamId(), strm.title(),
                            strm.category(), streamUrl,
                            tuple.getT1(), tuple.getT2(),
                            strm.authorId(), strm.desc(),
                            strm.created_at()
                        ));

                } catch (MalformedURLException e) {
                    return Mono.error(new RuntimeException("Invalid playback URL: " + strm.arn(), e));
                }
            });
    }

    /**
     * Fetches a stream by its ID. Uses either a presigned video link (if uploaded) or IVS playback URL (if live).
     *
     * @param streamId the stream ID
     * @param urlDuration optional link expiration duration
     * @return a Mono of {@link StreamingInfo} with all available metadata and URL
     */
    public Mono<StreamingInfo> fetchStreamById(int streamId, Optional<Duration> urlDuration) {
        return streamService.findFileById(streamId)
            .flatMap(strm -> {
                Mono<URL> streamUrlMono = getPresignedUrl(strm.object_key(), urlDuration);
                Mono<String> authorMono = userService.findUsernameById(strm.authorId());
                Mono<URL> thumbnailUrlMono = getPresignedUrl(strm.thumbnail_obj_key(), urlDuration);

                streamUrlMono.filter(url -> !url.equals(this.INVALIDURL))
                    .switchIfEmpty(Mono.fromCallable(() -> {
                                try {
                                    return new URL(ivs.getChannel(req -> req.arn(strm.arn()).build()).channel().playbackUrl());
                                } catch (MalformedURLException e) {
                                    throw new RuntimeException("Invalid playback URL: " + strm.arn(), e);
                                }
                            }));

                return Mono.zip(streamUrlMono, thumbnailUrlMono, authorMono)
                    .map(tuple -> new StreamingInfo(
                        strm.streamId(), strm.title(),
                        strm.category(), tuple.getT1(),
                        (tuple.getT2().equals(this.INVALIDURL)) ? null : tuple.getT2(),
                        tuple.getT3(), strm.authorId(),
                        strm.desc(), strm.created_at()
                        ));
            });
    }

    /**
     * Creates a presigned URL for the given S3 object key.
     *
     * @param objectKey the S3 object key (e.g., video or thumbnail)
     * @param duration optional duration before link expires
     * @return a Mono containing the presigned URL, or a url == this.INVALIDURL (google.com).
     */
    private Mono<URL> getPresignedUrl(String objectKey, Optional<Duration> duration) {
        if (objectKey == null) return Mono.just(INVALIDURL);
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
            log.info("Presigned URL (valid for " + duration.orElse(Duration.ofHours(2)).toMinutes() + " minutes): " + presignedUrl);
            return presignedUrl;
        });
    }

    /**
     * Cleans up AWS S3 presigner resources when the service is shut down.
     */
    @PreDestroy
    public void shutdown() {
        if (this.presigner != null) {
            presigner.close();
            log.info("S3Presigner closed");
        }
    }
}

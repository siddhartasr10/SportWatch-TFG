package com.reactive.SportWatch.services;

import java.time.LocalDateTime;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import com.reactive.SportWatch.models.Streaming;

@Service
public class StreamService {

    private static Logger log = Logger.getLogger(StreamService.class.toString());

    private final DatabaseClient dbClient;

    @Autowired
    public StreamService(DatabaseClient databaseClient) {
        this.dbClient = databaseClient;
    }

    // SELECT * FROM your_table
    // WHERE foo IS NULL OR foo = '';
    // Uploaded streams are the ones that have object_key
    public Flux<Streaming> findUploadedStreams() {
        Flux<Streaming> streamings = dbClient.sql("SELECT * FROM streams WHERE object_key IS NOT NULL OR object_key != ''")
                .map((row, metadata) -> new Streaming()
                        .streamId(row.get("stream_id", Integer.class))
                        .authorId(row.get("author_id", Integer.class))
                        .arn(row.get("arn", String.class))
                        .object_key(row.get("object_key", String.class))
                        .title(row.get("title", String.class))
                        .category(row.get("category", String.class))
                        .desc(row.get("description", String.class))
                        .created_at(row.get("created_at", LocalDateTime.class))
                        .thumbnail_obj_key(row.get("thumbnail_obj_key", String.class)))
                .all();

        return streamings;
    }

    // Streams that are still on as its object_key gets retrieved by the poller when it receives the create_object notification
    // NOTE: maybe the time at which the notification gets received is either very late or the poller very infrequent to the point
    // that some streams end and keep hanging until they're deemed 'ended' by the poller.
    public Flux<Streaming> findNonUploadedStreams() {
        Flux<Streaming> streamings = dbClient.sql("SELECT * FROM streams WHERE object_key IS NULL OR object_key = ''")
                .map((row, metadata) -> new Streaming()
                        .streamId(row.get("stream_id", Integer.class))
                        .authorId(row.get("author_id", Integer.class))
                        .arn(row.get("arn", String.class))
                        .title(row.get("title", String.class))
                        .category(row.get("category", String.class))
                        .desc(row.get("description", String.class))
                        .created_at(row.get("created_at", LocalDateTime.class))
                        .thumbnail_obj_key(row.get("thumbnail_obj_key", String.class)))
                .all();

        return streamings;
    }

    // Adds a stream to the db, intended to be called when someone starts streaming.
    // Leaves object_key empty so it gets completed after, and completes arn so the row can be searched after.
    public Mono<Streaming> createStream(Streaming streaming) {
        Mono<Streaming> stream = dbClient.sql
            ("INSERT INTO streams (author_id, arn, title, category, description) VALUES (:author_id, :arn, :title, :category, :description)")
            .bind("author_id", streaming.authorId())
            .bind("title", streaming.title())
            .bind("arn", streaming.arn())
            .bind("category", streaming.category())
            .bind("description", streaming.desc())
            .fetch().rowsUpdated()
            .map(changes -> {
                    switch(changes.intValue()) {
                        case 0 -> log.warning("No stream was inserted");
                        case 1 -> log.info("Stream inserted succesfully");
                    }
                    return streaming;
                });

        return stream;
    }
    // Searches an arn and an empty object_key to find the stream that has finished and add the file route to the
    // uploaded stream.
    public Mono<Streaming> updateStream(Streaming streaming) {
        log.info("Updating stream: " + streaming.title());
        log.info("Adding its recording master.m3u8 object key");
        Mono<Streaming> stream = dbClient.sql("""
                UPDATE streams SET object_key = :object_key WHERE object_key IS NULL OR object_key = ''
                AND arn = :arn
                """)
                .bind("object_key", streaming.object_key())
                .bind("arn", streaming.arn())
                .fetch().rowsUpdated()
                .map(changes -> {
                    switch (changes.intValue()) {
                        case 0 -> log.warning("No stream was updated");
                        case 1 -> log.info("Stream updated succesfully");
                    }
                    return streaming;
                });

        return stream;

    }

    
    public Mono<Streaming> addThumbnailToStream(Streaming streaming) {
        log.info("Updating stream: " + streaming.title());
        log.info("Adding its thumbnail");
        Mono<Streaming> stream = dbClient.sql("""
                UPDATE streams SET thumbnail_obj_key = :thumbnail_obj_key WHERE thumbnail_obj_key IS NULL OR thumbnail_obj_key = ''
                AND arn = :arn
                """)
                .bind("thumbnail_obj_key", streaming.thumbnail_obj_key())
                .bind("arn", streaming.arn())
                .fetch().rowsUpdated()
                .map(changes -> {
                    switch (changes.intValue()) {
                        case 0 -> log.warning("No stream was updated");
                        case 1 -> log.info("Stream updated succesfully");
                    }
                    return streaming;
                });

        return stream;
    }

    // Get the stream object_key by id 
    // if no object_key and thumbnail_obj_key was found it will return empty stream
    // if one of them was found it will return almost empty stream with the field.
    public Mono<Streaming> findFileById(int id) {
        log.info("Id passed to findFileById: " + Integer.toString(id));
        return dbClient.sql("SELECT * FROM streams WHERE stream_id = :stream_id")
            .bind("stream_id", id)
            .fetch().first()
            .map(res -> new Streaming()
                 .streamId(id)
                 .title((String) res.get("title"))
                 .arn(((String) res.get("arn")))
                 .object_key((String) res.get("object_key"))
                 .thumbnail_obj_key(((String) res.get("thumbnail_obj_key")))
                 .created_at((LocalDateTime) res.get("created_at"))
                 .category((String) res.get("category"))
                 .desc((String) res.get("description"))
                 .authorId(((Integer) res.get("author_id"))))
            .map(strm -> {log.info("Found strm: " + strm.toString()); return strm;})
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "No stream with that Id could be found")));
    };
}

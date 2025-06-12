package com.reactive.SportWatch.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

import com.reactive.SportWatch.models.Comment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;


@Service
public class CommentService {
    private DatabaseClient dbClient;

    private static final Logger log = Logger.getLogger(CommentService.class.getName());

    @Autowired
    CommentService(DatabaseClient dbClient) {
        this.dbClient = dbClient;
    }
    // \d comments
    //  comment_id | integer                     |              | not null | nextval('comments_
    //  author_id  | integer                     |              | not null |
    //  stream_id  | integer                     |              | not null |
    //  comment    | character varying(512)      |              | not null |
    //  created_at | timestamp without time zone |              | not null | CURRENT_TIMESTAMP

    public Mono<Comment> createComment(Comment comment) {
        return dbClient.sql("INSERT INTO comments (author_id, stream_id, comment) VALUES (:author_id, :stream_id, :comment)")
                .bind("author_id", comment.authorId())
                .bind("stream_id", comment.streamId())
                .bind("comment", comment.comment())
                .fetch().rowsUpdated()
                .map(changes -> {
                    if (changes > 1)
                        log.info("More than one row changed when creating a comment, check DB");
                    if (changes == 0)
                        log.info("No changes done when inserting comment into the db");
                    if (changes == 1)
                        log.info("Successfully created a comment");
                    return comment;
                });
    }

    public Mono<Comment> updateCommentMsg(Comment comment) {
        return dbClient.sql("UPDATE comments SET comment = :comment WHERE comment_id = :comment_id")
                .bind("comment_id", comment.commentId())
                .bind("comment", comment.comment())
                .fetch().rowsUpdated()
                .map(changes -> {
                    if (changes > 1)
                        log.info("More than one row changed when updating a comment msg, check DB");
                    if (changes == 0)
                        log.info("No changes done when updating comment msg");
                    if (changes == 1)
                        log.info("Successfully updated a comment msg");
                    return comment;
                });
    }

    public Mono<Comment> updateCommentMsg(Integer commentId, String comment) {
        return dbClient.sql("UPDATE comments SET comment = :comment WHERE comment_id = :comment_id")
                .bind("comment_id", commentId)
                .bind("comment", comment)
                .fetch().rowsUpdated()
                .map(changes -> {
                    if (changes > 1)
                        log.info("More than one row changed when updating a comment msg, check DB");
                    if (changes == 0)
                        log.info("No changes done when updating comment msg");
                    if (changes == 1)
                        log.info("Successfully updated a comment msg");
                    return new Comment().comment(comment).commentId(commentId);
                });
    }

    public Mono<Integer> deleteComment(Comment comment) {
        return dbClient.sql("DELETE FROM comments WHERE comment_id = :comment_id")
                .bind("comment_id", comment.commentId())
                .fetch().rowsUpdated()
                .map(changes -> {
                    if (changes > 1)
                        log.info("More than one row changed when removing a comment, check DB");
                    if (changes == 0)
                        log.info("No changes done when trying to remove comment");
                    if (changes == 1)
                        log.info("Successfully removed a comment");
                    return changes.intValue();
                });
    }

    public Mono<Integer> deleteComment(Integer commentId) {
        return dbClient.sql("DELETE FROM comments WHERE comment_id = :comment_id")
                .bind("comment_id", commentId)
                .fetch().rowsUpdated()
                .map(changes -> {
                    if (changes > 1)
                        log.info("More than one row changed when removing a comment, check DB");
                    if (changes == 0)
                        log.info("No changes done when trying to remove comment");
                    if (changes == 1)
                        log.info("Successfully removed a comment");
                    return changes.intValue();
                });
    }

    public Mono<Comment> findById(Integer commentId) {
        return dbClient.sql("SELECT * FROM comments WHERE comment_id = :comment_id")
            .bind("comment_id", commentId)
            .map((row, metadata) -> new Comment().commentId(row.get("comment_id", Integer.class))
                 .authorId(row.get("author_id", Integer.class))
                 .streamId(row.get("stream_id", Integer.class))
                 .comment(row.get("comment", String.class))
                 .createdAt(row.get("created_at", LocalDateTime.class)))
            .first();
    }

    public Mono<Integer> findAuthorById(Integer commentId) {
        return dbClient.sql("SELECT author_id FROM comments WHERE comment_id = :comment_id").bind("comment_id", commentId)
            .fetch().first().map(map -> (Integer) map.get("author_id"));
    }

    // Para escalar este tipo de funciones en aplicaciones grandes me imagino que hacen paginacion de la base de datos.
    // Es decir, hay rutas que los resultados de estas funciones los cortan un poco o incluso solo se traen los primeros x que se les pida
    // y permiten rangos.
    public Mono<List<Comment>> findByStreamId(Integer streamId) {
        return dbClient.sql("SELECT * FROM comments WHERE stream_id = :stream_id").bind("stream_id", streamId)
            .map(row -> new Comment().commentId(row.get("comment_id", Integer.class))
                 .authorId(row.get("author_id", Integer.class))
                 .streamId(row.get("stream_id", Integer.class))
                 .comment(row.get("comment", String.class))
                 .createdAt(row.get("created_at", LocalDateTime.class)))
            .all().collectList();
    }
}

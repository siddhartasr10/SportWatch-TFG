package com.reactive.SportWatch.models;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class Comment {
    @JsonProperty
    Integer commentId;

    @JsonProperty
    Integer authorId;

    @JsonProperty
    Integer streamId;

    @JsonProperty
    String comment;

    @JsonProperty
    LocalDateTime createdAt;

    public Comment() {}

    public Integer commentId() {
        return commentId;
    }

    public Integer authorId() {
        return authorId;
    }

    public Integer streamId() {
        return streamId;
    }

    public String comment() {
        return comment;
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }

    public Comment commentId(Integer commentId) {
        this.commentId = commentId;
        return this;
    }

    public Comment authorId(Integer authorId) {
        this.authorId = authorId;
        return this;
    }

    public Comment streamId(Integer streamId) {
        this.streamId = streamId;
        return this;
    }

    public Comment comment(String comment) {
        this.comment = comment;
        return this;
    }

    public Comment createdAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    @Override
    public String toString() {
        return "Comment [commentId=" + commentId + ", authorId=" + authorId + ", streamId=" + streamId + ", comment=" + comment + ", createdAt=" + createdAt + "]";
    }
}

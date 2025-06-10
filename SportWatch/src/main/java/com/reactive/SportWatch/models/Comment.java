package com.reactive.SportWatch.models;

import java.time.LocalDateTime;

public class Comment {
    public Comment() {}
    // Primary Key
    Integer commentId;
    Integer authorId;
    Integer streamId;

    String comment;

    LocalDateTime createdAt;

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
}

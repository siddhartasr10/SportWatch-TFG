package com.reactive.SportWatch.models;

import java.time.LocalDateTime;

public class Streaming {

    private int authorId;
    private String arn;
    // auto-record/<channel-id>/<recording-session-id>/recording.mp4
    // .mp4 Object key of the stream when it gets uploaded.
    private String object_key;
    private String title;
    private String category;
    private String desc;
    private LocalDateTime created_at;
    // ivs/v1/<aws_account_id>/<channel_id>/<year>/<month>/<day>/<hour>/<minute>/<recording_id>/media/thumbnails/<timestamp>.jpg
    // ivs/v1/123456789012/abcdEfghIJkl/2025/05/28/media/latest_thumbnail/thumb.jpg
    // .jpg Object key of the thumbnail when it gets uploaded.
    private String thumbnail_obj_key;

    public Streaming() {}

    public static Streaming stream() {
        return new Streaming();
    }

    public int authorId() {
        return authorId;
    }

    public String arn() {
        return arn;
    }

    //  channel arn: arn:aws:ivs:eu-west-1:123456789012:channel/AbCDeFGh1234
    public String channelId() {
        return arn.split("/")[1];
    }

    public String object_key() {
        return object_key;
    }

    public String title() {
        return title;
    }

    public String category() {
        return category;
    }

    public String desc() {
        return desc;
    }

    public LocalDateTime created_at() {
        return created_at;
    }

    public String thumbnail_obj_key() {
        return thumbnail_obj_key;
    }

    public Streaming authorId(int authorId) {
        this.authorId = authorId;
        return this;
    }

    public Streaming arn(String arn) {
        this.arn = arn;
        return this;
    }

    public Streaming object_key(String objectKey) {
        this.object_key = objectKey;
        return this;
    }

    public Streaming title(String title) {
        this.title = title;
        return this;
    }

    public Streaming category(String category) {
        this.category = category;
        return this;
    }

    public Streaming desc(String desc) {
        this.desc = desc;
        return this;
    }

    public Streaming created_at(LocalDateTime created_at) {
        this.created_at = created_at;
        return this;
    }

    public Streaming thumbnail_obj_key(String thumbnail_obj_key) {
        this.thumbnail_obj_key = thumbnail_obj_key;
        return this;
    }


}

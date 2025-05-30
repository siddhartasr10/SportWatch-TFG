package com.reactive.SportWatch.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class S3EventJson {
    public List<Record> Records;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Record {
        public S3Entity S3;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class S3Entity {
            public Bucket bucket;
            public S3Object object;

            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Bucket {
                public String name;
            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class S3Object {
                public String key;
                public long size;
            }
        }
    }
}

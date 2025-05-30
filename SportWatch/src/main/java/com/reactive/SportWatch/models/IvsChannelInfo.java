package com.reactive.SportWatch.models;

public record IvsChannelInfo(String streamKey, String rtmpsUrl) {

    public String toString() {
        return String.format("<StreamKey>: %s, <RtmpsUrl>: %s", streamKey(), rtmpsUrl());
    }

}

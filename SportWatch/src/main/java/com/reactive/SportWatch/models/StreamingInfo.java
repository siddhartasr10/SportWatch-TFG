package com.reactive.SportWatch.models;

import java.net.URL;
import java.time.LocalDateTime;

// Info thats going to be sent to the frontend.
// streamUrl can be playbackUrl or presignedUrl of an uploaded stream so .m3u8 or .mp4
// thumbnailUrl always is presigned url
public record StreamingInfo(String title, String category,
                            URL streamUrl, URL thumbnailUrl,
                            int authorId, String desc,
                            LocalDateTime createdAt) {}

package com.nlu.app.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Document(collection = "comments")
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class Comment {
    @Id
    String id;
    String videoId;
    String userId;
    String content;
    Long timestamp;
    String parentId;

    // Getters and Setters

    public LocalDateTime getTimestamp() {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault());
    }

    public void setTimestamp(LocalDateTime localDateTime) {
        this.timestamp = localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond();
    }
}

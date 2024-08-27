package com.nlu.app.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Document(collection = "comments")
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    @Id
    String id = UUID.randomUUID().toString();
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

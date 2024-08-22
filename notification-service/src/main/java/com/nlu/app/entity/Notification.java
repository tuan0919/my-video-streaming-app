package com.nlu.app.entity;

import com.nlu.app.constant.NotificationType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Document(collection = "notifications")
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class Notification {
    @Id
    String id = UUID.randomUUID().toString();
    String userId;
    Long timestamp;
    String content;
    Boolean isRead;
    @Field("type")
    NotificationType type;

    public LocalDateTime getTimestamp() {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault());
    }

    public void setTimestamp(LocalDateTime localDateTime) {
        this.timestamp = localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond();
    }
}

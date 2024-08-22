package com.nlu.app.entity;

import com.nlu.app.constant.NotificationType;
import lombok.*;
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
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    String id = UUID.randomUUID().toString();
    String userId;
    @Field("timestamp")
    Long time = System.currentTimeMillis();
    String content;
    Boolean isRead = false;
    @Field("type")
    NotificationType type;

    public LocalDateTime getTimestamp() {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(time), ZoneId.systemDefault());
    }

    public void setTimestamp(LocalDateTime localDateTime) {
        this.time = localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond();
    }
}

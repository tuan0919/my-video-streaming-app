package com.nlu.app.entity;

import com.nlu.app.constant.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String notificationId;
    String userId;
    LocalDateTime time = LocalDateTime.now();
    String content;
    String title;
    String relatedObjectId;
    Boolean isRead = false;
    @Enumerated(EnumType.STRING)
    NotificationType type;
    String relatedEvent;
}

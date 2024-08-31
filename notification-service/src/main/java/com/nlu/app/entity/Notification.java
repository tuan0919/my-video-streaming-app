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
@Entity(name = "notifications")
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String userId;
    LocalDateTime time;
    String content;
    Boolean isRead = false;
    @Enumerated(EnumType.STRING)
    NotificationType type;
}

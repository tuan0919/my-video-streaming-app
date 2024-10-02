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
public class CommentNotification {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String notificationId;
    String commentId;
}

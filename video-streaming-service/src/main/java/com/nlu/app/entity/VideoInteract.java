package com.nlu.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity(name = "video_interactions")
@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class VideoInteract {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    @ManyToOne
    @JoinColumn(name = "videoId")
    Video video;
    String userId;
    Float progress;
    String vote; // possible action: NO_ACTION, UP_VOTE, DOWN_VOTE
    LocalDateTime updateTime;
}

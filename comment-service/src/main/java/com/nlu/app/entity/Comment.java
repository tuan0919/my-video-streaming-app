package com.nlu.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String videoId;
    String userId;
    String content;
    @Builder.Default
    LocalDateTime createAt = LocalDateTime.now();
    LocalDateTime updateAt = LocalDateTime.now();
    @OneToOne
    @JoinColumn(name = "parent_comment_id", nullable = true)
    @Builder.Default
    Comment parent = null;
}

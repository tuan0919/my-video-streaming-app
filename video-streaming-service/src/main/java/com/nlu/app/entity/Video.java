package com.nlu.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Entity(name = "videos")
@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String videoId;
    @Column
    String userId;
    @Column(columnDefinition = "datetime default now()")
    LocalDateTime createAt;
    @Column(columnDefinition = "varchar(255) collate utf8mb4_unicode_ci not null")
    String videoKey;
    @Column(columnDefinition = "varchar(255) collate utf8mb4_unicode_ci not null")
    String thumbnailKey;
    String videoDescription;
    String videoName;
    @Builder.Default
    Integer viewCount = 0;
    @OneToMany
    Set<VideoInteract> interactions;
}

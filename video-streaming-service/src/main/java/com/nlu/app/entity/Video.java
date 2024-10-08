package com.nlu.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "videos")
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
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
    @OneToMany(mappedBy = "video", cascade = CascadeType.ALL, orphanRemoval = true)

    Set<VideoInteract> interactions = new HashSet<>();
    @Override
    public String toString() {
        return "Video{" +
                "videoId='" + videoId + '\'' +
                ", userId='" + userId + '\'' +
                ", videoName='" + videoName + '\'' +
                ", viewCount=" + viewCount +
                '}';
    }
}

package com.nlu.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
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
    @Column(name="tags")
    String tagsString;
    @Transient @Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE)
    List<String> tags;
    @PostLoad
    private void loadTags() {
        this.tags = Arrays.asList(tagsString.split(","));
    }
    @PrePersist
    @PreUpdate
    private void saveTags() {
        this.tagsString = String.join(",", tags);
    }
}

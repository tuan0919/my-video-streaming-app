package com.nlu.app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "comments")
@Getter
@Setter
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
    @ManyToOne
    @JoinColumn(name = "parent_comment_id", nullable = true, unique = false)
    @Builder.Default
    Comment parent = null;
    @OneToMany(mappedBy = "parent")
    @ToString.Exclude
    @JsonIgnore
    Set<Comment> reply = new HashSet<>();
    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    Set<CommentInteract> interactions = new HashSet<>();
    @Column(columnDefinition = "default 0")
    Integer likeCounts = 0;
    @Column(columnDefinition = "default 0")
    Integer dislikeCounts = 0;
}

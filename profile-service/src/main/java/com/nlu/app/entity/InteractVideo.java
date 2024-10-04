package com.nlu.app.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "user_interact_videos")
@Data
public class InteractVideo {
    @Id
    String id;
    @ManyToOne
    Profile profile;
    String videoId;
    String interact; // UP VOTE, DOWN VOTE
    Long watchedDuration;
    LocalDateTime updateAt;
}

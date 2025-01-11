package com.nlu.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;
import java.util.Set;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "profile")
@Data
public class Profile {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    String profileId;
    @Column
    String userId;
    @Column
    String fullName;
    @Column
    String country;
    @Column (columnDefinition = "text")
    String bio;
    @Column
    String avatarId;
    @Column
    String address;
    @Column(columnDefinition = "BOOLEAN DEFAULT 1")
    boolean gender;
    @ManyToMany
    @JoinTable(
            name = "profile_follow",
            joinColumns = @JoinColumn(name = "follower_id"),
            inverseJoinColumns = @JoinColumn(name = "followed_id")
    )
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Set<Profile> follow;
    @ManyToMany(mappedBy = "follow")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Set<Profile> followers;
}

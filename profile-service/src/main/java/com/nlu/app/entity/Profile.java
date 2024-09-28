package com.nlu.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}

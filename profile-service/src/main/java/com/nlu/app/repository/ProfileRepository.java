package com.nlu.app.repository;

import com.nlu.app.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, String> {
    Optional<Profile> findProfileByUserId(String userId);
    List<Profile> findProfilesByUserIdIn(List<String> userIds);
}

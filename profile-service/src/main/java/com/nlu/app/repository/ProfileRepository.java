package com.nlu.app.repository;

import com.nlu.app.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, String> {
    Optional<Profile> findProfileByUserId(String userId);
}

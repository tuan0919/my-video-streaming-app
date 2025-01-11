package com.nlu.app.repository;

import com.nlu.app.entity.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, String> {
    Optional<Profile> findProfileByUserId(String userId);
    List<Profile> findProfilesByUserIdIn(List<String> userIds);
    @Query("""
        SELECT v 
        FROM profile p JOIN p.savedVideoIds v
        WHERE p.profileId = :profileId
    """)
    Page<String> findSavedVideosByProfileId(@Param("profileId") String profileId, Pageable pageable);
}

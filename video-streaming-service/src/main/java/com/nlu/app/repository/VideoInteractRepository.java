package com.nlu.app.repository;

import com.nlu.app.entity.VideoInteract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VideoInteractRepository extends JpaRepository<VideoInteract, String> {
    Optional<VideoInteract> findByVideoVideoIdAndUserId(String videoId, String userId);
}

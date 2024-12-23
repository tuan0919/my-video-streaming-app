package com.nlu.app.repository;

import com.nlu.app.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, String> {
    List<Notification> findAllByUserId(String userId);
    Integer countAllByUserIdAndAndIsRead(String userId, boolean isRead);
}
package com.nlu.app.repository;

import com.nlu.app.entity.CommentNotification;
import com.nlu.app.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentNotificationRepository extends JpaRepository<CommentNotification, String> {
}
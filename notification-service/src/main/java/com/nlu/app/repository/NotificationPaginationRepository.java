package com.nlu.app.repository;

import com.nlu.app.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationPaginationRepository extends PagingAndSortingRepository<Notification, String> {
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId")
    Page<Notification> findAllByUserId(Pageable pageable, @Param("userId") String userId);
}
package com.nlu.app.repository;

import com.nlu.app.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface CommentPaginationRepository extends PagingAndSortingRepository<Comment, String> {
    Page<Comment> findCommentByVideoIdAndParentIsNull(String userId, Pageable pageable);
}
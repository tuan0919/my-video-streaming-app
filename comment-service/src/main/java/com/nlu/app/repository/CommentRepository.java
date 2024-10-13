package com.nlu.app.repository;

import com.nlu.app.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, String> {
    List<Comment> findCommentsByParent_Id(String parentId);
    List<Comment> findCommentsByVideoIdAndParentIsNull(String videoId);
}
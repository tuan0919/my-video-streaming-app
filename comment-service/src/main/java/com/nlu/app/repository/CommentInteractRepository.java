package com.nlu.app.repository;

import com.nlu.app.entity.Comment;
import com.nlu.app.entity.CommentInteract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentInteractRepository extends JpaRepository<CommentInteract, String> {
    Optional<CommentInteract> findByComment_IdAndUserId(String commentId, String userId);
    @Query("SELECT ci FROM CommentInteract ci WHERE ci.userId = :userId AND ci.comment.id IN :commentIds")
    List<CommentInteract> findUserInteractForComments(@Param("userId") String userId, @Param("commentIds") List<String> commentIds);
    Integer countByComment_IdAndAction(String commentId, String action);
}
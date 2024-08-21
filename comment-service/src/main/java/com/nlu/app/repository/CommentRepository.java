package com.nlu.app.repository;

import com.nlu.app.entity.Comment;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface CommentRepository extends ReactiveMongoRepository<Comment, String> {
    Flux<Comment> findByVideoId(String videoId);
    Flux<Comment> findByUserId(String userId);
    Flux<Comment> findByParentId(String parentId); // Truy vấn các reply theo comment cha
}
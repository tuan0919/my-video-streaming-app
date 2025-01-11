package com.nlu.app.repository;

import com.nlu.app.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPaginationRepository extends PagingAndSortingRepository<User, String> {
    @Query("""
    SELECT u.id FROM User u
    WHERE u.username LIKE %:username%
""")
    Page<String> searchUserIdByUsername(String username, Pageable pageable);
}

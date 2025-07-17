package com.nlu.app.repository;

import com.nlu.app.entity.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProfilePaginationRepository extends PagingAndSortingRepository<Profile, String> {
    @Query("""
        SELECT p FROM profile p
        ORDER BY p.profileId ASC
    """)
    Page<Profile> suggestProfile(Pageable pageable);
}

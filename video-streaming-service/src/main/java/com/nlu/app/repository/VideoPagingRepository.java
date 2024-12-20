package com.nlu.app.repository;

import com.nlu.app.entity.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface VideoPagingRepository extends PagingAndSortingRepository<Video, String> {
    @Query("""
        SELECT v.videoId
        FROM videos v
        LEFT JOIN v.interactions vi
        GROUP BY v
        ORDER BY COALESCE(
        SUM(CASE WHEN vi.vote = 'UP_VOTE' THEN 1 ELSE 0 END)
            -
        SUM(CASE WHEN vi.vote = 'DOWN_VOTE' THEN 1 ELSE 0 END), 0)
        DESC
    """)
    Page<String> fetchFromStart(Pageable pageable);
}

package com.nlu.app.repository;

import com.nlu.app.entity.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

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
        DESC, v.videoId ASC
    """)
    Page<String> fetchFromStart(Pageable pageable);

    @Query("""
        SELECT v.videoId
        FROM videos v
        LEFT JOIN v.interactions vi
        WHERE v.videoId != :excludeId
        GROUP BY v
        ORDER BY COALESCE(
        SUM(CASE WHEN vi.vote = 'UP_VOTE' THEN 1 ELSE 0 END)
            -
        SUM(CASE WHEN vi.vote = 'DOWN_VOTE' THEN 1 ELSE 0 END), 0)
        DESC, v.videoId ASC
    """)
    Page<String> fetchFromStartExcludeId(Pageable pageable, @Param("excludeId") String id);
    @Query("""
        SELECT v.videoId
        FROM videos v
        WHERE v.videoName LIKE %:title%
    """)
    Page<String> searchVideoIdByName(@Param("title") String title, Pageable pageable);
}

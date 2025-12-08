package com.example.demo.repository;

import com.example.demo.entity.PlayHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PlayHistoryRepository extends JpaRepository<PlayHistory, Long> {
    
    // 특정 사용자의 최근 재생 기록 조회 (최신순)
    @Query(value = "SELECT * FROM PLAY_HISTORY WHERE USER_ID = :userId ORDER BY PLAYED_AT DESC", nativeQuery = true)
    List<PlayHistory> findByUserIdOrderByPlayedAtDesc(@Param("userId") Long userId);

    // 특정 사용자의 최근 재생 기록 조회 (제한된 개수)
    @Query(value = "SELECT * FROM (SELECT * FROM PLAY_HISTORY WHERE USER_ID = :userId ORDER BY PLAYED_AT DESC) WHERE ROWNUM <= :limit", nativeQuery = true)
    List<PlayHistory> findRecentByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    // 특정 사용자의 특정 곡 재생 기록 삭제
    void deleteByUserIdAndSongId(Long userId, Long songId);
}


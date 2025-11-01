package com.example.demo.repository;

import com.example.demo.entity.PlaylistSong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional; // 🚨 Transactional import

import java.util.List;

public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, Long> {

    List<PlaylistSong> findByPlaylistIdOrderBySongOrderAsc(Long playlistId);

    @Modifying
    @Query("DELETE FROM PlaylistSong ps WHERE ps.playlistId = :playlistId")
    void deleteAllByPlaylistId(@Param("playlistId") Long playlistId);

    // 🚨 [새로 추가] playlistId와 songId로 특정 곡만 삭제
    // delete...By...는 JPA 규칙에 따라 DELETE 쿼리를 생성합니다.
    // 트랜잭션 안에서 실행되어야 하므로 @Transactional 추가
    @Transactional
    void deleteByPlaylistIdAndSongId(Long playlistId, Long songId);
}
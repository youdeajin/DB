package com.example.demo.repository;

import com.example.demo.entity.PlaylistSong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, Long> {

    // 🚨 [새로 추가] 특정 playlistId에 해당하는 곡 목록을 songOrder 오름차순으로 조회
    List<PlaylistSong> findByPlaylistIdOrderBySongOrderAsc(Long playlistId);

    // 🚨 [새로 추가] 특정 playlistId에 해당하는 모든 데이터를 삭제하는 JPQL 쿼리
    // @Modifying 어노테이션은 INSERT, UPDATE, DELETE 쿼리 실행 시 필요합니다.
    @Modifying
    @Query("DELETE FROM PlaylistSong ps WHERE ps.playlistId = :playlistId")
    void deleteAllByPlaylistId(@Param("playlistId") Long playlistId);

}
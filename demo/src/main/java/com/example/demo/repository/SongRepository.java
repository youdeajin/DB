package com.example.demo.repository;

import com.example.demo.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // 🚨 추가
import org.springframework.data.repository.query.Param; // 🚨 추가
import java.util.List;

public interface SongRepository extends JpaRepository<Song, Long> {

    // 제목으로 검색
    List<Song> findByTitleContainingIgnoreCase(String titleKeyword);

    // 장르로 검색
    List<Song> findByGenreContainingIgnoreCase(String genre);

    // 아티스트 ID로 검색
    List<Song> findByArtistId(Long artistId);

    // 앨범 ID로 검색
    List<Song> findByAlbumIdOrderBySongIdAsc(Long albumId);

    // 제목과 아티스트 ID로 곡 존재 여부 확인
    boolean existsByTitleAndArtistId(String title, Long artistId);
    
    // 정확한 제목으로 곡 찾기
    List<Song> findByTitle(String title);

    // 🚨 [새로 추가] 랜덤 추천곡 조회 (Oracle 전용)
    @Query(value = "SELECT * FROM (SELECT * FROM SONGS ORDER BY DBMS_RANDOM.VALUE) WHERE ROWNUM <= :limit", nativeQuery = true)
    List<Song> findRandomSongs(@Param("limit") int limit);

    // 🚨 [새로 추가] 인기곡 조회 (임시로 ID순 정렬)
    @Query(value = "SELECT * FROM (SELECT * FROM SONGS ORDER BY song_id ASC) WHERE ROWNUM <= :limit", nativeQuery = true)
    List<Song> findPopularSongs(@Param("limit") int limit);
    // 🚨 [새로 추가] 최신순(SongId 내림차순)으로 12개 조회

    List<Song> findTop16ByOrderBySongIdDesc();
}
package com.example.demo.repository;

import com.example.demo.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SongRepository extends JpaRepository<Song, Long> {

    // 제목으로 검색 (기존 코드)
    List<Song> findByTitleContainingIgnoreCase(String titleKeyword);

    // 장르로 검색 (기존 코드)
    List<Song> findByGenreContainingIgnoreCase(String genre);

    // 아티스트 ID로 검색 (기존 코드)
    List<Song> findByArtistId(Long artistId);

    // 앨범 ID로 검색 (기존 코드)
    List<Song> findByAlbumIdOrderBySongIdAsc(Long albumId);

    // 🚨 [필수 추가] 제목과 아티스트 ID로 곡 존재 여부 확인 (SpotifyService에서 사용)
    boolean existsByTitleAndArtistId(String title, Long artistId);
}
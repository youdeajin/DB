package com.example.demo.repository;

import com.example.demo.entity.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional; // 🚨 Optional import 추가

public interface AlbumRepository extends JpaRepository<Album, Long> {

    // 🚨 [필수 추가] 제목과 아티스트 ID로 앨범 검색 (SpotifyService에서 사용)
    Optional<Album> findByTitleAndArtistId(String title, Long artistId);
}

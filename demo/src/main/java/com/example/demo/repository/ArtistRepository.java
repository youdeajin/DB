package com.example.demo.repository;

import com.example.demo.entity.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional; // 🚨 Optional import 추가

public interface ArtistRepository extends JpaRepository<Artist, Long> {

    // 🚨 [필수 추가] 이름으로 아티스트 검색 (SpotifyService에서 사용)
    Optional<Artist> findByName(String name);
}

package com.example.demo.service;

import com.example.demo.entity.Artist;
import com.example.demo.repository.ArtistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List; // 👈 List import 추가

@Service
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository artistRepository;

    /**
     * ID로 아티스트 정보를 조회합니다.
     * @param artistId 조회할 아티스트 ID
     * @return Artist 엔티티
     * @throws IllegalArgumentException 해당 ID의 아티스트가 없을 경우
     */
    @Transactional(readOnly = true)
    public Artist findArtistById(Long artistId) {
        return artistRepository.findById(artistId)
                .orElseThrow(() -> new IllegalArgumentException("Artist not found with ID: " + artistId));
    }
    /**
     * 🚨 [새로 추가] 모든 아티스트 목록을 조회합니다.
     * @return 모든 Artist 엔티티 목록
     */
    @Transactional(readOnly = true)
    public List<Artist> findAllArtists() {
        return artistRepository.findAll(); // JpaRepository의 findAll() 사용
    }
}
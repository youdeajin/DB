package com.example.demo.service;

import com.example.demo.entity.Song;
import com.example.demo.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections; // Collections import
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final SongRepository songRepository;

    /**
     * 특정 장르의 곡들을 조회하고 무작위로 섞어서 반환합니다.
     * @param genre 추천받고 싶은 장르 이름
     * @param limit 반환할 최대 곡 수 (선택 사항)
     * @return 섞인 Song 목록
     */
    @Transactional(readOnly = true)
    public List<Song> recommendByGenre(String genre, Integer limit) {
        // 1. 장르로 곡 목록 조회
        List<Song> songsByGenre = songRepository.findByGenreContainingIgnoreCase(genre);

        // 2. 결과 목록 셔플 (무작위 순서)
        Collections.shuffle(songsByGenre);

        // 3. limit 파라미터가 있으면 해당 개수만큼만 반환
        if (limit != null && limit > 0 && limit < songsByGenre.size()) {
            return songsByGenre.subList(0, limit);
        }

        return songsByGenre;
    }
}
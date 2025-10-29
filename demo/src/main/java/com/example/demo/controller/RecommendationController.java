package com.example.demo.controller;

import com.example.demo.entity.Song;
import com.example.demo.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations") // 기본 경로 /api/recommendations
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    /**
     * 장르 기반 추천 곡 목록 API (GET /api/recommendations/genre)
     * @param genre 필수 쿼리 파라미터 (예: ?genre=K-Pop)
     * @param limit 선택적 쿼리 파라미터 (예: &limit=5, 반환할 곡 수 제한)
     * @return 섞인 Song 목록과 200 OK 상태 코드
     */
    @GetMapping("/genre")
    public ResponseEntity<List<Song>> recommendByGenre(
            @RequestParam String genre,
            @RequestParam(required = false) Integer limit) { // limit은 필수가 아님

        List<Song> recommendedSongs = recommendationService.recommendByGenre(genre, limit);
        return ResponseEntity.ok(recommendedSongs);
    }
}
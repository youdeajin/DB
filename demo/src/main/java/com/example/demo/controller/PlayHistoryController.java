package com.example.demo.controller;

import com.example.demo.entity.Song;
import com.example.demo.service.PlayHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/play-history")
@RequiredArgsConstructor
public class PlayHistoryController {

    private final PlayHistoryService playHistoryService;

    /**
     * 재생 기록 저장 API (POST /api/play-history)
     */
    @PostMapping
    public ResponseEntity<?> recordPlay(@RequestBody Map<String, Long> request) {
        Long userId = request.get("userId");
        Long songId = request.get("songId");

        if (userId == null || songId == null) {
            return ResponseEntity.badRequest().body("userId and songId are required");
        }

        try {
            playHistoryService.recordPlay(userId, songId);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to record play: " + e.getMessage());
        }
    }

    /**
     * 특정 사용자의 최근 재생 곡 목록 조회 API (GET /api/play-history/user/{userId})
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Song>> getRecentSongs(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "50") int limit) {
        try {
            List<Song> recentSongs = playHistoryService.getRecentSongs(userId, limit);
            // 빈 리스트도 정상 응답으로 반환
            return ResponseEntity.ok(recentSongs != null ? recentSongs : List.of());
        } catch (Exception e) {
            // 에러 발생 시 로그 출력 후 빈 리스트 반환
            System.err.println("최근 재생 기록 조회 실패: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(List.of());
        }
    }
}


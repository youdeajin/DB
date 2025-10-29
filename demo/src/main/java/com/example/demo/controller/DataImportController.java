package com.example.demo.controller;

import com.example.demo.entity.Song; // Song 엔티티 import
import com.example.demo.service.SpotifyService; // SpotifyService import
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections; // 빈 리스트 반환 시 사용
import java.util.List;

/**
 * 외부 데이터 소스(예: Spotify)로부터 데이터를 가져와 DB에 저장하는 API 컨트롤러입니다.
 */
@RestController
@RequestMapping("/api/import") // 이 컨트롤러의 모든 API는 /api/import 로 시작합니다.
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성합니다 (Lombok).
public class DataImportController {

    // SpotifyService를 주입받아 사용합니다.
    private final SpotifyService spotifyService;

    /**
     * Spotify API를 호출하여 곡을 검색하고, 검색 결과를 로컬 데이터베이스에 저장합니다.
     * 이 API는 POST 요청으로 /api/import/spotify 경로를 통해 접근할 수 있습니다.
     *
     * @param query 검색할 키워드 (URL 파라미터로 필수: ?query=아이유)
     * @param limit 가져올 최대 곡 수 (URL 파라미터로 선택: &limit=5, 기본값은 10)
     * @return DB에 새로 저장된 곡 목록 (JSON 형태) 또는 오류 시 빈 목록과 함께 적절한 상태 코드.
     */
    @PostMapping("/spotify") // POST 요청을 /spotify 경로에 매핑합니다.
    public ResponseEntity<List<Song>> importFromSpotify(
            @RequestParam String query, // 'query' URL 파라미터를 필수로 받습니다.
            @RequestParam(defaultValue = "10") int limit) { // 'limit' URL 파라미터를 받으며, 없으면 기본값 10을 사용합니다.

        try {
            // SpotifyService의 메서드를 호출하여 실제 검색 및 저장 작업을 수행합니다.
            List<Song> importedSongs = spotifyService.searchAndSaveTracks(query, limit);

            // 작업 성공 시, 저장된 곡 목록과 함께 200 OK 응답을 반환합니다.
            return ResponseEntity.ok(importedSongs);
        } catch (Exception e) {
            // SpotifyService에서 오류 발생 시 (예: API 인증 실패, 네트워크 오류 등)
            System.err.println("Spotify 데이터 가져오기 실패: " + e.getMessage());
            e.printStackTrace(); // 서버 로그에 전체 오류 스택 출력 (디버깅용)

            // 클라이언트에게는 500 Internal Server Error와 빈 목록을 반환합니다.
            // 좀 더 구체적인 오류 응답을 DTO로 만들어 반환할 수도 있습니다.
            return ResponseEntity.internalServerError().body(Collections.emptyList());
        }
    }
}


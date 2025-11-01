package com.example.demo.controller;

import com.example.demo.dto.BatchImportRequest; // 🚨 일괄 가져오기 요청 DTO
import com.example.demo.entity.Song; // Song 엔티티
import com.example.demo.service.SpotifyService; // Spotify 서비스
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping; // POST 매핑
import org.springframework.web.bind.annotation.RequestBody; // @RequestBody
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam; // @RequestParam
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList; // ArrayList 사용
import java.util.Collections; // Collections.emptyList() 사용
import java.util.List; // List 사용

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
     * [단일 검색] Spotify API를 호출하여 곡을 검색하고 결과를 로컬 DB에 저장합니다.
     * @param query 검색할 키워드 (필수)
     * @param limit 가져올 최대 곡 수 (선택, 기본값 10)
     * @return DB에 새로 저장된 곡 목록 (JSON)
     */
    @PostMapping("/spotify") // POST /api/import/spotify?query=...&limit=...
    public ResponseEntity<List<Song>> importFromSpotify(
            @RequestParam String query, // URL 쿼리 파라미터 'query'
            @RequestParam(defaultValue = "10") int limit) { // URL 쿼리 파라미터 'limit', 없으면 10

        try {
            List<Song> importedSongs = spotifyService.searchAndSaveTracks(query, limit);
            // 성공 시 200 OK 와 함께 저장된 곡 목록 반환
            return ResponseEntity.ok(importedSongs);
        } catch (Exception e) {
            System.err.println("Spotify (단일) 데이터 가져오기 실패: " + e.getMessage());
            e.printStackTrace();
            // 오류 발생 시 500 Internal Server Error와 빈 목록 반환
            return ResponseEntity.internalServerError().body(Collections.emptyList());
        }
    }

    /**
     * 🚨 [일괄 검색] Spotify에서 여러 검색어로 일괄 검색하여 DB에 저장하는 API
     * @param request 검색어 목록(queries)과 키워드당 제한(limitPerQuery)이 담긴 DTO
     * @return DB에 새로 저장된 모든 곡의 목록
     */
    @PostMapping("/spotify-batch") // POST /api/import/spotify-batch
    public ResponseEntity<List<Song>> importBatchFromSpotify(
            @RequestBody BatchImportRequest request) { // 🚨 JSON Body로 받음
        
        List<Song> totalImportedSongs = new ArrayList<>(); // 모든 결과를 담을 리스트
        
        // 유효성 검사: 쿼리 목록이 없거나 비어있으면 400 Bad Request 반환
        if (request.getQueries() == null || request.getQueries().isEmpty()) {
             return ResponseEntity.badRequest().body(totalImportedSongs);
        }

        // 유효성 검사: limit 값이 0 이하면 기본값 10 사용
        int limit = request.getLimitPerQuery() > 0 ? request.getLimitPerQuery() : 10; 

        System.out.println("일괄 가져오기 시작... 총 " + request.getQueries().size() + "개의 키워드.");

        // 2. 요청받은 키워드 목록을 하나씩 순회
        for (String query : request.getQueries()) {
            if (query == null || query.trim().isEmpty()) continue; // 빈 키워드는 건너뛰기

            System.out.println("'" + query + "' 검색 및 저장 중...");
            try {
                // 3. 기존 SpotifyService의 검색 및 저장 메서드 호출
                List<Song> importedSongs = spotifyService.searchAndSaveTracks(query, limit);
                // 결과 리스트에 추가
                totalImportedSongs.addAll(importedSongs);
            } catch (Exception e) {
                // 특정 키워드 검색 실패 시 서버 로그에만 남기고 계속 진행
                // (하나의 키워드가 실패해도 전체 작업이 중단되지 않도록)
                System.err.println("'" + query + "' 검색 중 오류 발생: " + e.getMessage());
            }
        }

        System.out.println("일괄 가져오기 완료. 총 " + totalImportedSongs.size() + "곡 저장됨.");
        // 4. 모든 결과를 모아서 200 OK 응답 반환
        return ResponseEntity.ok(totalImportedSongs);
    }
}


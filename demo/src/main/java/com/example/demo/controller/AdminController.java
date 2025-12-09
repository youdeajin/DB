package com.example.demo.controller;

import com.example.demo.dto.AdminAuthRequest;
import com.example.demo.dto.SongUpdateRequest;
import com.example.demo.entity.Song;
import com.example.demo.service.AdminService;
import com.example.demo.service.SongService;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;
    private final SongService songService;

    /**
     * 관리자 인증
     */
    @PostMapping("/auth")
    public ResponseEntity<Map<String, Object>> authenticate(@RequestBody AdminAuthRequest request) {
        try {
            boolean isAuthenticated = adminService.authenticate(request);
            if (isAuthenticated) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "인증 성공");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "비밀번호가 일치하지 않습니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "인증 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 모든 사용자 조회
     */
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("users", userService.findAllUsers());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "사용자 목록 조회 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 사용자 삭제
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "사용자 삭제 완료");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "사용자 삭제 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 모든 곡 조회 (관리자용)
     */
    @GetMapping("/songs")
    public ResponseEntity<Map<String, Object>> getAllSongs() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("songs", songService.findAllSongs());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "곡 목록 조회 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 곡 정보 수정
     */
    @PutMapping("/songs/{songId}")
    public ResponseEntity<Map<String, Object>> updateSong(
            @PathVariable Long songId,
            @RequestBody SongUpdateRequest request) {
        try {
            // 기존 곡 조회
            Song existingSong = songService.findSongById(songId);
            
            // 수정할 필드만 업데이트
            if (request.getTitle() != null) {
                existingSong.setTitle(request.getTitle());
            }
            if (request.getArtistId() != null) {
                existingSong.setArtistId(request.getArtistId());
            }
            if (request.getAlbumId() != null) {
                existingSong.setAlbumId(request.getAlbumId());
            }
            if (request.getFilePath() != null) {
                existingSong.setFilePath(request.getFilePath());
            }
            if (request.getDurationSeconds() != null) {
                existingSong.setDurationSeconds(request.getDurationSeconds());
            }
            if (request.getGenre() != null) {
                existingSong.setGenre(request.getGenre());
            }

            Song updatedSong = songService.updateSong(songId, existingSong);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "곡 정보 수정 완료");
            response.put("song", updatedSong);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "곡 정보 수정 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 곡 삭제
     */
    @DeleteMapping("/songs/{songId}")
    public ResponseEntity<Map<String, Object>> deleteSong(@PathVariable Long songId) {
        try {
            songService.deleteSong(songId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "곡 삭제 완료");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "곡 삭제 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}


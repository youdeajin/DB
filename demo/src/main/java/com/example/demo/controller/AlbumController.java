package com.example.demo.controller;

import com.example.demo.entity.Album;
import com.example.demo.entity.Song;
import com.example.demo.entity.UserSavedAlbum;
import com.example.demo.repository.UserSavedAlbumRepository;
import com.example.demo.service.AlbumService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;
    private final UserSavedAlbumRepository userSavedAlbumRepository; // 🚨 [추가] 앨범 저장 레포지토리 주입

    /**
     * 모든 앨범 조회 API (GET /api/albums)
     */
    @GetMapping
    public ResponseEntity<List<Album>> getAllAlbums() {
        return ResponseEntity.ok(albumService.findAllAlbums());
    }

    /**
     * 특정 앨범의 수록곡 조회 API (GET /api/albums/{albumId}/songs)
     */
    @GetMapping("/{albumId}/songs")
    public ResponseEntity<List<Song>> getAlbumSongs(@PathVariable Long albumId) {
        return ResponseEntity.ok(albumService.findSongsByAlbumId(albumId));
    }

    // --- 🚨 아래부터 새로 추가된 기능 ---

    /**
     * 앨범 저장 (좋아요/찜하기) API (POST /api/albums/{albumId}/save)
     * Body: { "userId": 1 }
     */
    @PostMapping("/{albumId}/save")
    public ResponseEntity<?> saveAlbum(@PathVariable Long albumId, @RequestBody Map<String, Long> body) {
        Long userId = body.get("userId");
        if (userId == null) {
            return ResponseEntity.badRequest().body("User ID is required");
        }

        // 이미 저장되어 있는지 확인 후 저장
        if (userSavedAlbumRepository.findByUserIdAndAlbumId(userId, albumId).isEmpty()) {
            userSavedAlbumRepository.save(new UserSavedAlbum(userId, albumId));
            return ResponseEntity.ok("Album saved successfully");
        } else {
            return ResponseEntity.ok("Album already saved");
        }
    }

    /**
     * 앨범 저장 취소 API (DELETE /api/albums/{albumId}/save?userId={userId})
     */
    @DeleteMapping("/{albumId}/save")
    @Transactional
    public ResponseEntity<?> unsaveAlbum(@PathVariable Long albumId, @RequestParam Long userId) {
        if (userId == null) {
            return ResponseEntity.badRequest().body("User ID is required");
        }
        
        userSavedAlbumRepository.deleteByUserIdAndAlbumId(userId, albumId);
        return ResponseEntity.ok("Album unsaved successfully");
    }

    /**
     * 사용자가 저장한 앨범 ID 목록 조회 API (GET /api/albums/saved/ids?userId={userId})
     * - 프론트엔드에서 하트(♥) 표시를 활성화할 때 사용
     */
    @GetMapping("/saved/ids")
    public ResponseEntity<List<Long>> getSavedAlbumIds(@RequestParam Long userId) {
        List<Long> ids = userSavedAlbumRepository.findByUserId(userId).stream()
                .map(UserSavedAlbum::getAlbumId)
                .toList();
        return ResponseEntity.ok(ids);
    }
}
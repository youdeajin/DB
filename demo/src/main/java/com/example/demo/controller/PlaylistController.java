package com.example.demo.controller;

import com.example.demo.dto.PlaylistDetailResponse;
import com.example.demo.dto.PlaylistRequest;
import com.example.demo.dto.PlaylistSongsUpdateRequest;
import com.example.demo.entity.Playlist;
import com.example.demo.service.PlaylistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    /**
     * 재생목록 생성 API (POST /api/playlists)
     * - 요청 바디에 포함된 userId를 사용하여 재생목록을 생성합니다.
     */
    @PostMapping
    public ResponseEntity<Playlist> createPlaylist(@RequestBody PlaylistRequest request) {
        // 🚨 [수정] 요청에서 받은 userId 사용 (없으면 400 에러)
        Long userId = request.getUserId();
        if (userId == null) {
             return ResponseEntity.badRequest().build(); // 로그인 안 된 상태로 요청 시 거부
        }

        Playlist createdPlaylist = playlistService.createPlaylist(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPlaylist);
    }

    /**
     * 🚨 [새로 추가] 특정 사용자의 재생목록만 조회하는 API (GET /api/playlists/user/{userId})
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Playlist>> getUserPlaylists(@PathVariable Long userId) {
        List<Playlist> playlists = playlistService.findUserPlaylists(userId);
        return ResponseEntity.ok(playlists);
    }

    /**
     * 모든 재생목록 조회 API (GET /api/playlists)
     */
    @GetMapping
    public ResponseEntity<List<Playlist>> getAllPlaylists() {
        // 이 API는 모든 (공개된) 재생목록을 조회하는 용도로 사용될 수 있습니다.
        return ResponseEntity.ok(playlistService.findAllPlaylists());
    }

    /**
     * 특정 재생목록 상세 조회 API (GET /api/playlists/{playlistId})
     */
    @GetMapping("/{playlistId}")
    public ResponseEntity<PlaylistDetailResponse> getPlaylistById(@PathVariable Long playlistId) {
        try {
            PlaylistDetailResponse playlistDetail = playlistService.findPlaylistById(playlistId);
            return ResponseEntity.ok(playlistDetail);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 재생목록 삭제 API (DELETE /api/playlists/{playlistId})
     */
    @DeleteMapping("/{playlistId}")
    public ResponseEntity<Void> deletePlaylist(@PathVariable Long playlistId) {
        try {
            playlistService.deletePlaylist(playlistId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 재생목록 곡 목록 전체 수정 API (PUT /api/playlists/{playlistId}/songs)
     */
    @PutMapping("/{playlistId}/songs")
    public ResponseEntity<Void> updatePlaylistSongs(
            @PathVariable Long playlistId,
            @RequestBody PlaylistSongsUpdateRequest request) {
        try {
            playlistService.updatePlaylistSongs(playlistId, request);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("Error updating playlist songs: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 재생목록에 곡 하나 추가 API (POST /api/playlists/{playlistId}/songs)
     */
    @PostMapping("/{playlistId}/songs")
    public ResponseEntity<Void> addSongToPlaylist(
            @PathVariable Long playlistId,
            @RequestBody Map<String, Long> songData) {
        Long songId = songData.get("songId");
        if (songId == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            playlistService.addSongToPlaylist(playlistId, songId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
             if (e.getMessage().contains("Playlist not found") || e.getMessage().contains("Song not found")) {
                 return ResponseEntity.notFound().build();
             }
             System.out.println("Add song failed: " + e.getMessage());
             return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 재생목록에서 특정 곡 삭제 API (DELETE /api/playlists/{playlistId}/songs/{songId})
     */
    @DeleteMapping("/{playlistId}/songs/{songId}")
    public ResponseEntity<Void> removeSongFromPlaylist(
            @PathVariable Long playlistId,
            @PathVariable Long songId) {
        try {
            playlistService.removeSongFromPlaylist(playlistId, songId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.err.println("재생목록 곡 삭제 중 오류: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
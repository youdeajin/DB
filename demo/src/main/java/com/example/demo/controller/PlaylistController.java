package com.example.demo.controller;

import com.example.demo.dto.PlaylistRequest;
import com.example.demo.entity.Playlist;
import com.example.demo.service.PlaylistService;
import com.example.demo.dto.PlaylistDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import com.example.demo.dto.PlaylistTitleUpdateRequest; // 🚨 새 DTO import
import com.example.demo.dto.PlaylistSongsUpdateRequest;

@RestController
@RequestMapping("/api/playlists") // 기본 경로 /api/playlists
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    /**
     * 재생목록 생성 API (POST /api/playlists)
     * - 인증을 제거하고 임시 사용자 ID(1L)를 사용합니다.
     * @param request 재생목록 제목, 공개 여부, 곡 ID 목록
     * @return 생성된 Playlist 객체와 201 Created 상태 코드
     */
    @PostMapping
    public ResponseEntity<Playlist> createPlaylist(
            @RequestBody PlaylistRequest request) { // Authentication 파라미터 제거됨

        // 임시 사용자 ID를 1로 설정합니다.
        // DB에 USER_ID=1인 사용자가 있는지 확인해야 합니다.
        Long tempUserId = 1L;

        // 서비스 로직 호출 시 임시 사용자 ID를 전달합니다.
        Playlist createdPlaylist = playlistService.createPlaylist(request, tempUserId);

        // 생성 성공 시 201 Created 상태 코드와 함께 생성된 재생목록 정보 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPlaylist);
    }

    /**
     * 재생목록 조회 API (GET /api/playlists)
     * - 인증을 제거하고 임시 사용자 ID(1L)의 재생목록을 조회합니다.
     * @return 해당 사용자의 Playlist 목록과 200 OK 상태 코드
     */
    @GetMapping
    public ResponseEntity<List<Playlist>> getAllPlaylists() { // 메서드 이름 변경
        // 서비스의 findAllPlaylists() 호출
        List<Playlist> playlists = playlistService.findAllPlaylists();
        return ResponseEntity.ok(playlists);
    }
    /**
     * 🚨 [새로 추가] 특정 재생목록 상세 조회 API (GET /api/playlists/{playlistId})
     * @param playlistId 경로 변수(Path Variable)로 전달된 재생목록 ID
     * @return PlaylistDetailResponse DTO와 200 OK 상태 코드
     */
    @GetMapping("/{playlistId}")
    public ResponseEntity<PlaylistDetailResponse> getPlaylistById(@PathVariable Long playlistId) {
        try {
            PlaylistDetailResponse playlistDetail = playlistService.findPlaylistById(playlistId);
            return ResponseEntity.ok(playlistDetail);
        } catch (IllegalArgumentException e) {
            // 해당 ID의 재생목록이 없을 경우 404 Not Found 반환
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * @param playlistId 경로 변수로 전달된 삭제할 재생목록 ID
     * @return 성공 시 204 No Content 상태 코드
     */
    @DeleteMapping("/{playlistId}")
    public ResponseEntity<Void> deletePlaylist(@PathVariable Long playlistId) {
        try {
            playlistService.deletePlaylist(playlistId);
            // 삭제 성공 시 내용 없이 204 No Content 응답 반환
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            // 해당 ID의 재생목록이 없을 경우 404 Not Found 반환
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            // 기타 서버 오류 시 500 Internal Server Error 반환
            return ResponseEntity.internalServerError().build();
        }
    }

   /**
     * 🚨 [새로 추가] 특정 재생목록 곡 목록 수정 API (PUT /api/playlists/{playlistId}/songs)
     * @param playlistId 경로 변수로 전달된 수정할 재생목록 ID
     * @param request 새 곡 ID 목록을 담은 DTO
     * @return 성공 시 200 OK 상태 코드 (내용 없음)
     */
    @PutMapping("/{playlistId}/songs")
    public ResponseEntity<Void> updatePlaylistSongs(
            @PathVariable Long playlistId,
            @RequestBody PlaylistSongsUpdateRequest request) {
        try {
            playlistService.updatePlaylistSongs(playlistId, request);
            // 성공 시 200 OK 응답 반환
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            // 해당 ID의 재생목록이 없을 경우 404 Not Found 반환
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            // 기타 서버 오류 시 500 Internal Server Error 반환
            System.err.println("Error updating playlist songs: " + e.getMessage()); // 디버깅용 로그
            e.printStackTrace(); // 스택 트레이스 출력
            return ResponseEntity.internalServerError().build();
        }
 
    }
    // PlaylistController.java 파일에 추가

    /**
     * 특정 재생목록에 곡 하나를 추가하는 API (POST /api/playlists/{playlistId}/songs)
     * Request Body에는 {"songId": 123} 형태의 JSON 예상
     * @param playlistId 곡을 추가할 재생목록 ID
     * @param songData 추가할 곡 ID를 담은 Map 또는 DTO
     * @return 성공 시 200 OK
     */
    @PostMapping("/{playlistId}/songs") // 기존 PUT과 경로가 같지만 메서드가 다름
    public ResponseEntity<Void> addSongToPlaylist(
            @PathVariable Long playlistId,
            @RequestBody Map<String, Long> songData) { // 간단히 Map으로 받기
        Long songId = songData.get("songId");
        if (songId == null) {
            return ResponseEntity.badRequest().build(); // songId 누락 시 오류
        }
        try {
            playlistService.addSongToPlaylist(playlistId, songId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            // 재생목록이나 곡 ID가 잘못된 경우
             if (e.getMessage().contains("Playlist not found") || e.getMessage().contains("Song not found")) {
                 return ResponseEntity.notFound().build();
             }
             // 이미 곡이 있는 경우 등 다른 IllegalArgumentException 처리 (선택적)
             // return ResponseEntity.status(HttpStatus.CONFLICT).build(); // 예: 409 Conflict
             System.out.println("Add song failed: " + e.getMessage());
             return ResponseEntity.badRequest().body(null); // 단순 Bad Request로 처리
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    /**
     * 🚨 [새로 추가] 특정 재생목록에서 특정 곡 삭제 API
     * (DELETE /api/playlists/{playlistId}/songs/{songId})
     * @param playlistId 경로 변수로 전달된 재생목록 ID
     * @param songId 경로 변수로 전달된 삭제할 곡 ID
     * @return 성공 시 204 No Content
     */
    @DeleteMapping("/{playlistId}/songs/{songId}")
    public ResponseEntity<Void> removeSongFromPlaylist(
            @PathVariable Long playlistId,
            @PathVariable Long songId) {
        
        try {
            playlistService.removeSongFromPlaylist(playlistId, songId);
            // 삭제 성공 시 (존재하지 않는 ID였어도 쿼리는 성공) 204 No Content 반환
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            // DB 제약 조건 오류 등 예기치 않은 오류 발생 시
            System.err.println("재생목록 곡 삭제 중 오류: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
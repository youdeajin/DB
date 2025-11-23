package com.example.demo.controller;

import com.example.demo.entity.Song;
import com.example.demo.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;

    // 전체 곡 조회
    @CrossOrigin(origins = "http://localhost:3000") 
    @GetMapping
    public ResponseEntity<List<Song>> getAllSongs() {
        List<Song> songs = songService.findAllSongs();
        return ResponseEntity.ok(songs);
    }
    
    // 🚨 [새로 추가] 랜덤 추천곡 API (GET /api/songs/random)
    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/random")
    public ResponseEntity<List<Song>> getRandomSongs(@RequestParam(defaultValue = "10") int limit) {
        List<Song> songs = songService.findRandomSongs(limit);
        return ResponseEntity.ok(songs);
    }

    // 🚨 [새로 추가] 인기곡 API (GET /api/songs/popular)
    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/popular")
    public ResponseEntity<List<Song>> getPopularSongs(@RequestParam(defaultValue = "10") int limit) {
        List<Song> songs = songService.findPopularSongs(limit);
        return ResponseEntity.ok(songs);
    }
    
    // 특정 곡 정보 조회
    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/{songId}")
    public ResponseEntity<Song> getSongDetail(@PathVariable Long songId) {
        try {
            Song song = songService.findSongById(songId);
            return ResponseEntity.ok(song);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 곡 검색 API
    @GetMapping("/search")
    public ResponseEntity<List<Song>> searchSongs(@RequestParam String query) {
        List<Song> searchResults = songService.searchSongsByTitle(query);
        return ResponseEntity.ok(searchResults);
    }

    // 🚨 [새로 추가] 최신 곡 API
    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/recent")
    public ResponseEntity<List<Song>> getRecentSongs() {
        // Service를 거치지 않고 Repository를 바로 호출해도 되지만, 
        // 정석대로 Service에 위임하려면 Service에도 메서드를 추가해야 합니다.
        // 여기서는 편의상 Service에 추가했다고 가정하고 호출하거나, 
        // 간단하게 Repository를 직접 호출하는 코드로 알려드릴게요.
        // (SongService에 findTop12ByOrderBySongIdDesc를 호출하는 findRecentSongs 메서드를 추가해주세요!)
        
        // * Service 파일 수정이 번거로우시다면 아래 로직을 SongService.java에 추가하세요:
        // public List<Song> findRecentSongs() { return songRepository.findTop12ByOrderBySongIdDesc(); }
        
        return ResponseEntity.ok(songService.findRecentSongs());
    }
}
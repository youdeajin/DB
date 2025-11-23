package com.example.demo.controller;

import com.example.demo.entity.Artist;
import com.example.demo.service.ArtistService;
import com.example.demo.entity.Song;
import com.example.demo.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;

@RestController
@RequestMapping("/api/artists") // 기본 경로 /api/artists
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService artistService;
    private final SongService songService;

    /**
     * 특정 아티스트 정보 조회 API (GET /api/artists/{artistId})
     * @param artistId 경로 변수로 전달된 아티스트 ID
     * @return Artist 엔티티와 200 OK 상태 코드
     */
    @GetMapping("/{artistId}")
    public ResponseEntity<Artist> getArtistById(@PathVariable Long artistId) {
        try {
            Artist artist = artistService.findArtistById(artistId);
            return ResponseEntity.ok(artist);
        } catch (IllegalArgumentException e) {
            // 해당 ID의 아티스트가 없을 경우 404 Not Found 반환
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 🚨 [새로 추가] 특정 아티스트의 모든 곡 목록 조회 API (GET /api/artists/{artistId}/songs)
     * @param artistId 경로 변수로 전달된 아티스트 ID
     * @return 해당 아티스트의 Song 목록과 200 OK 상태 코드
     */
    @GetMapping("/{artistId}/songs")
    public ResponseEntity<List<Song>> getSongsByArtistId(@PathVariable Long artistId) {
        // SongService를 사용하여 해당 아티스트의 곡 목록 조회
        List<Song> songs = songService.findSongsByArtistId(artistId);
        // 결과가 비어있더라도 200 OK와 빈 배열 반환
        return ResponseEntity.ok(songs);
    }

    /**
     * 🚨 [새로 추가] 모든 아티스트 목록 조회 API (GET /api/artists)
     * @return 모든 Artist 엔티티 목록과 200 OK 상태 코드
     */
    @GetMapping // 👈 경로 변수 없이 GetMapping만 사용
    public ResponseEntity<List<Artist>> getAllArtists() {
        List<Artist> artists = artistService.findAllArtists();
        return ResponseEntity.ok(artists);
    }
}
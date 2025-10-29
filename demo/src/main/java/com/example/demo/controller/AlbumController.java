package com.example.demo.controller;

import com.example.demo.entity.Album;
import com.example.demo.entity.Song;
import com.example.demo.service.AlbumService;
import com.example.demo.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;


@RestController
@RequestMapping("/api/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;
    private final SongService songService;

    // 특정 앨범 정보 조회 (기존 코드)
    @GetMapping("/{albumId}")
    public ResponseEntity<Album> getAlbumById(@PathVariable Long albumId) {
        // ... (기존 로직)
         try {
            Album album = albumService.findAlbumById(albumId);
            return ResponseEntity.ok(album);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 특정 앨범의 곡 목록 조회 (기존 코드)
    @GetMapping("/{albumId}/songs")
    public ResponseEntity<List<Song>> getSongsByAlbumId(@PathVariable Long albumId) {
        // ... (기존 로직)
         List<Song> songs = songService.findSongsByAlbumId(albumId);
        return ResponseEntity.ok(songs);
    }

    /**
     * 🚨 [새로 추가] 모든 앨범 목록 조회 API (GET /api/albums)
     * @return 모든 Album 엔티티 목록과 200 OK 상태 코드
     */
    @GetMapping
    public ResponseEntity<List<Album>> getAllAlbums() {
        List<Album> albums = albumService.findAllAlbums();
        return ResponseEntity.ok(albums);
    }
}
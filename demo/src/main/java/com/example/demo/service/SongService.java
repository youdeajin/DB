package com.example.demo.service;

import com.example.demo.entity.Song;
import com.example.demo.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SongService {

    private final SongRepository songRepository;

    // 모든 곡 목록 조회
    @Transactional(readOnly = true)
    public List<Song> findAllSongs() {
        return songRepository.findAll();
    }

    // 특정 곡 상세 정보 조회
    @Transactional(readOnly = true)
    public Song findSongById(Long songId) {
        return songRepository.findById(songId)
                .orElseThrow(() -> new IllegalArgumentException("Song not found with ID: " + songId));
    }

    // 곡 제목 검색
    @Transactional(readOnly = true)
    public List<Song> searchSongsByTitle(String query) {
        return songRepository.findByTitleContainingIgnoreCase(query);
    }

    @Transactional(readOnly = true)
    public List<Song> findSongsByArtistId(Long artistId) {
        return songRepository.findByArtistId(artistId);
    }

    @Transactional(readOnly = true)
    public List<Song> findSongsByAlbumId(Long albumId) {
        return songRepository.findByAlbumIdOrderBySongIdAsc(albumId);
    }

    // 🚨 [새로 추가] 랜덤 곡 서비스 로직
    @Transactional(readOnly = true)
    public List<Song> findRandomSongs(int limit) {
        return songRepository.findRandomSongs(limit);
    }

    // 🚨 [새로 추가] 인기곡 서비스 로직
    @Transactional(readOnly = true)
    public List<Song> findPopularSongs(int limit) {
        return songRepository.findPopularSongs(limit);
    }

    // SongService.java 내부
    // 🚨 [수정] 최신 곡 조회 서비스 (Top16 호출)
    @Transactional(readOnly = true)
    public List<Song> findRecentSongs() {
        return songRepository.findTop16ByOrderBySongIdDesc();
    }
}
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

    // 모든 곡 목록 조회 (재생목록 구성에 사용)
    @Transactional(readOnly = true)
    public List<Song> findAllSongs() {
        return songRepository.findAll();
    }

    // 특정 곡 상세 정보 조회 (재생 요청 시 사용)
    @Transactional(readOnly = true)
    public Song findSongById(Long songId) {
        return songRepository.findById(songId)
                .orElseThrow(() -> new IllegalArgumentException("Song not found with ID: " + songId));
    }

    /**
     * 🚨 [새로 추가] 곡 제목으로 곡을 검색합니다 (대소문자 무시).
     * @param query 검색 키워드
     * @return 검색된 Song 목록
     */
    @Transactional(readOnly = true)
    public List<Song> searchSongsByTitle(String query) {
        // 제목으로만 검색하는 Repository 메서드 호출
        return songRepository.findByTitleContainingIgnoreCase(query);
    }

    @Transactional(readOnly = true)
    public List<Song> findSongsByArtistId(Long artistId) {
        // 아티스트 ID로 곡 목록을 조회하는 Repository 메서드 호출
        return songRepository.findByArtistId(artistId);
    }

    @Transactional(readOnly = true)
    public List<Song> findSongsByAlbumId(Long albumId) {
        // 앨범 ID로 곡 목록을 조회하는 Repository 메서드 호출
        return songRepository.findByAlbumIdOrderBySongIdAsc(albumId);
    }
}
package com.example.demo.service;

import com.example.demo.entity.Album;
import com.example.demo.entity.Song;
import com.example.demo.repository.AlbumRepository;
import com.example.demo.repository.SongRepository; // 🚨 [필수] SongRepository 임포트
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final SongRepository songRepository; // 🚨 [필수] SongRepository 주입

    @Transactional(readOnly = true)
    public List<Album> findAllAlbums() {
        return albumRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Album> findAlbumById(Long albumId) {
        return albumRepository.findById(albumId);
    }
    
    /**
     * 특정 앨범 ID로 수록곡 목록을 조회합니다.
     * AlbumController에서 호출하는 메서드입니다.
     */
    @Transactional(readOnly = true)
    public List<Song> findSongsByAlbumId(Long albumId) {
        // SongRepository의 findByAlbumIdOrderBySongIdAsc 메서드를 사용
        return songRepository.findByAlbumIdOrderBySongIdAsc(albumId);
    }
}
package com.example.demo.service;

import com.example.demo.entity.Album;
import com.example.demo.repository.AlbumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List; // 👈 List import 추가

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository;

    // ID로 앨범 조회 (기존 코드)
    @Transactional(readOnly = true)
    public Album findAlbumById(Long albumId) {
        return albumRepository.findById(albumId)
                .orElseThrow(() -> new IllegalArgumentException("Album not found with ID: " + albumId));
    }

    // 🚨 [새로 추가] 모든 앨범 목록 조회
    @Transactional(readOnly = true)
    public List<Album> findAllAlbums() {
        return albumRepository.findAll(); // JpaRepository 기본 메서드 활용
    }
}
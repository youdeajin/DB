package com.example.demo.repository;

import com.example.demo.entity.UserSavedAlbum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserSavedAlbumRepository extends JpaRepository<UserSavedAlbum, Long> {
    // 특정 사용자가 저장한 앨범 목록 조회 (ID만 필요 시)
    List<UserSavedAlbum> findByUserId(Long userId);
    
    // 특정 사용자가 특정 앨범을 저장했는지 확인
    Optional<UserSavedAlbum> findByUserIdAndAlbumId(Long userId, Long albumId);
    
    // 저장 취소 (삭제)
    @Transactional
    void deleteByUserIdAndAlbumId(Long userId, Long albumId);
}
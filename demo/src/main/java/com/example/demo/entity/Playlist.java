package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter; // 🚨 Setter import 추가 (없었다면)
import java.time.LocalDateTime;

@Entity
@Table(name = "PLAYLISTS")
@Getter
@Setter // 🚨 이 어노테이션이 누락되었을 가능성이 높습니다. 추가하세요.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "playlist_seq_gen")
    @SequenceGenerator(name = "playlist_seq_gen", sequenceName = "PLAYLISTS_SEQ", allocationSize = 1)
    private Long playlistId;

    private String title; // ⬅️ 이 필드를 위한 setTitle 메서드가 필요합니다.

    @Column(name = "OWNER_USER_ID")
    private Long ownerUserId;

    @Column(name = "IS_PUBLIC")
    private Integer isPublic;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    // 수동 생성자 (생성 시 사용)
    public Playlist(String title, Long ownerUserId, Integer isPublic, LocalDateTime createdAt) {
        this.title = title;
        this.ownerUserId = ownerUserId;
        this.isPublic = isPublic;
        this.createdAt = createdAt;
    }

    // 전체 필드 생성자 (JPA 로드 시 사용)
    public Playlist(Long playlistId, String title, Long ownerUserId, Integer isPublic, LocalDateTime createdAt) {
        this.playlistId = playlistId;
        this.title = title;
        this.ownerUserId = ownerUserId;
        this.isPublic = isPublic;
        this.createdAt = createdAt;
    }
    
    // 💡 Lombok @Setter를 사용하지 않으려면 이 메서드를 직접 추가해도 됩니다.
    // public void setTitle(String title) {
    //     this.title = title;
    // }
}
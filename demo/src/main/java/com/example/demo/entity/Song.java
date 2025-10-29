package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
// import lombok.Setter; // 필요 시 추가

@Entity
@Table(name = "SONGS") // DB 테이블 이름 명시
@Getter
// @Setter // 필요 시 추가
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "song_seq_gen")
    @SequenceGenerator(name = "song_seq_gen", sequenceName = "SONGS_SEQ", allocationSize = 1)
    private Long songId;

    @Column(nullable = false)
    private String title;

    @Column(name = "ARTIST_ID")
    private Long artistId;

    @Column(name = "ALBUM_ID")
    private Long albumId;

    @Column(name = "FILE_PATH", length = 512, nullable = false) // 길이 명시 및 nullable=false
    private String filePath;

    @Column(name = "DURATION_SECONDS")
    private Integer durationSeconds; // 🚨 타입을 Integer로 확인

    private String genre;

    // 🚨 [필수 확인/추가] SpotifyService에서 사용할 생성자
    public Song(String title, Long artistId, Long albumId, String filePath, Integer durationSeconds, String genre) {
        this.title = title;
        this.artistId = artistId;
        this.albumId = albumId;
        this.filePath = filePath;
        this.durationSeconds = durationSeconds;
        this.genre = genre;
    }

    // JPA가 모든 필드를 로드하기 위한 생성자 (기존 확인)
    public Song(Long songId, String title, Long artistId, Long albumId, String filePath, Integer durationSeconds, String genre) {
        this.songId = songId;
        this.title = title;
        this.artistId = artistId;
        this.albumId = albumId;
        this.filePath = filePath;
        this.durationSeconds = durationSeconds;
        this.genre = genre;
    }
}
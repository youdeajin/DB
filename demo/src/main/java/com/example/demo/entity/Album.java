package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter; // Setter 추가 (커버 URL 업데이트용)
import java.time.LocalDate; // 날짜 타입

@Entity
@Table(name = "ALBUMS") // DB 테이블 이름 명시
@Getter
@Setter // 필드 수정을 위해 Setter 추가
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "album_seq_gen")
    @SequenceGenerator(name = "album_seq_gen", sequenceName = "ALBUMS_SEQ", allocationSize = 1)
    private Long albumId;

    @Column(nullable = false)
    private String title;

    @Column(name = "ARTIST_ID")
    private Long artistId;

    @Column(name = "RELEASE_DATE")
    private LocalDate releaseDate;

    // --- 새로 추가된 필드 ---
    @Column(name = "ALBUM_COVER_URL", length = 512) // DB 컬럼명 매핑, 길이 지정
    private String coverUrl; // 앨범 커버 이미지 URL

    // --- 생성자 수정 ---
    // JPA 로드용 전체 필드 생성자
    public Album(Long albumId, String title, Long artistId, LocalDate releaseDate, String coverUrl) { // coverUrl 파라미터 추가
        this.albumId = albumId;
        this.title = title;
        this.artistId = artistId;
        this.releaseDate = releaseDate;
        this.coverUrl = coverUrl; // 필드 초기화 추가
    }

    // 데이터 삽입 시 사용할 생성자 (ID 제외)
    public Album(String title, Long artistId, LocalDate releaseDate, String coverUrl) { // coverUrl 파라미터 추가
        this.title = title;
        this.artistId = artistId;
        this.releaseDate = releaseDate;
        this.coverUrl = coverUrl; // 필드 초기화 추가
    }
}

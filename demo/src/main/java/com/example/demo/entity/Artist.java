package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
// import lombok.Setter; // 필요 시 추가

@Entity
@Table(name = "ARTISTS") // DB 테이블 이름 명시
@Getter
// @Setter // 필요 시 추가 (수정 기능을 넣을 때)
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "artist_seq_gen")
    @SequenceGenerator(name = "artist_seq_gen", sequenceName = "ARTISTS_SEQ", allocationSize = 1)
    private Long artistId;

    @Column(nullable = false)
    private String name;

    // JPA가 모든 필드를 로드하기 위한 생성자 (수동 추가)
    public Artist(Long artistId, String name) {
        this.artistId = artistId;
        this.name = name;
    }

    // 데이터 삽입 시 사용할 생성자 (선택 사항)
    public Artist(String name) {
        this.name = name;
    }
}
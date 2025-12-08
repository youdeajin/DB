package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "PLAY_HISTORY")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlayHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "play_history_seq_gen")
    @SequenceGenerator(name = "play_history_seq_gen", sequenceName = "PLAY_HISTORY_SEQ", allocationSize = 1)
    @Column(name = "PLAY_HISTORY_ID")
    private Long playHistoryId;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "SONG_ID")
    private Long songId;

    @Column(name = "PLAYED_AT")
    private LocalDateTime playedAt;

    // 재생 기록 생성자
    public PlayHistory(Long userId, Long songId, LocalDateTime playedAt) {
        this.userId = userId;
        this.songId = songId;
        this.playedAt = playedAt;
    }

    // 전체 필드 생성자 (JPA 로드 시 사용)
    public PlayHistory(Long playHistoryId, Long userId, Long songId, LocalDateTime playedAt) {
        this.playHistoryId = playHistoryId;
        this.userId = userId;
        this.songId = songId;
        this.playedAt = playedAt;
    }
}


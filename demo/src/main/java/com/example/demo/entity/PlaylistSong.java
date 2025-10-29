package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "PLAYLIST_SONGS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaylistSong {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "playlist_songs_seq_gen")
    @SequenceGenerator(name = "playlist_songs_seq_gen", sequenceName = "PLAYLIST_SONGS_SEQ", allocationSize = 1)
    private Long playlistSongId;

    @Column(name = "PLAYLIST_ID")
    private Long playlistId;
    
    @Column(name = "SONG_ID")
    private Long songId;
    
    @Column(name = "SONG_ORDER")
    private Integer songOrder;
    
    // 수동 생성자
    public PlaylistSong(Long playlistId, Long songId, Integer songOrder) {
        this.playlistId = playlistId;
        this.songId = songId;
        this.songOrder = songOrder;
    }

    // 전체 필드 생성자
    public PlaylistSong(Long playlistSongId, Long playlistId, Long songId, Integer songOrder) {
        this.playlistSongId = playlistSongId;
        this.playlistId = playlistId;
        this.songId = songId;
        this.songOrder = songOrder;
    }
}
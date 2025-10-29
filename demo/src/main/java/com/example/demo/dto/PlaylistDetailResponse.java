package com.example.demo.dto;

import com.example.demo.entity.Playlist;
import com.example.demo.entity.Song; // Song 엔티티 import
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List; // List import

@Getter
@Setter
public class PlaylistDetailResponse {
    private Long playlistId;
    private String title;
    private LocalDateTime createdAt;
    private List<Song> songs; // 🚨 재생목록에 포함된 곡 목록

    // Playlist 엔티티와 Song 리스트를 받아 DTO를 생성하는 생성자
    public PlaylistDetailResponse(Playlist playlist, List<Song> songs) {
        this.playlistId = playlist.getPlaylistId();
        this.title = playlist.getTitle();
        this.createdAt = playlist.getCreatedAt();
        this.songs = songs;
    }
}
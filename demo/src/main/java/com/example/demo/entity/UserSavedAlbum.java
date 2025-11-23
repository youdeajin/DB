package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "USER_SAVED_ALBUMS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSavedAlbum {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "usa_seq_gen")
    @SequenceGenerator(name = "usa_seq_gen", sequenceName = "USER_SAVED_ALBUMS_SEQ", allocationSize = 1)
    private Long savedId;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "ALBUM_ID")
    private Long albumId;

    @Column(name = "SAVED_AT")
    private LocalDateTime savedAt;

    public UserSavedAlbum(Long userId, Long albumId) {
        this.userId = userId;
        this.albumId = albumId;
        this.savedAt = LocalDateTime.now();
    }
}
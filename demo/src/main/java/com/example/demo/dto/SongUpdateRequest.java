package com.example.demo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SongUpdateRequest {
    private String title;
    private Long artistId;
    private Long albumId;
    private String filePath;
    private Integer durationSeconds;
    private String genre;
}


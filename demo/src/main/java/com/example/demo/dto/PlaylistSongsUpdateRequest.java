package com.example.demo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor // JSON 파싱용 기본 생성자
public class PlaylistSongsUpdateRequest {
    // 재생목록에 포함될 새로운 곡 ID 목록 전체
    private List<Long> songIds;
}
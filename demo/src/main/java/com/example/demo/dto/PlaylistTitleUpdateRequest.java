package com.example.demo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor // JSON 파싱을 위한 기본 생성자
public class PlaylistTitleUpdateRequest {
    private String newTitle; // 사용자가 변경하려는 새 제목
}
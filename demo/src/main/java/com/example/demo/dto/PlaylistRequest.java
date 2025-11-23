package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter @Setter
public class PlaylistRequest {
    private String title;
    private Boolean isPublic;
    private List<Long> songIds;
    private Long userId; // 🚨 [추가] 생성 요청한 사용자 ID
}
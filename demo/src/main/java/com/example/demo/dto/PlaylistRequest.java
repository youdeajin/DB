package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class PlaylistRequest {
    
    private String title;
    private Boolean isPublic; // 프론트엔드에서 받은 boolean
    private List<Long> songIds; // 재생목록에 추가할 곡 ID 목록
}

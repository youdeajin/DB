package com.example.demo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor // JSON 파싱을 위한 기본 생성자
public class BatchImportRequest {
    // 🚨 검색할 키워드 목록
    private List<String> queries;
    
    // 🚨 각 키워드당 검색할 곡 수 (선택 사항)
    private int limitPerQuery = 10; // 기본값 10
}

package com.example.demo.controller;

import com.example.demo.dto.ChatRequest;
import com.example.demo.entity.Song; // 🚨 Song 임포트
import com.example.demo.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.util.List; // 🚨 List 임포트
import java.util.Collections; // 🚨 Collections 임포트

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    /**
     * 🚨 [수정] AI 추천 API (반환 타입을 List<Song>으로 변경)
     */
    @PostMapping("/recommend")
    public ResponseEntity<List<Song>> getMusicRecommendation(@RequestBody ChatRequest chatRequest) {
        try {
            // ChatbotService가 이제 Song 리스트를 반환
            List<Song> recommendation = chatbotService.getRecommendation(chatRequest.getPrompt());
            return ResponseEntity.ok(recommendation); // 🚨 Song 리스트 반환
        } catch (Exception e) {
            e.printStackTrace();
            // 오류 시 500 에러와 빈 리스트 반환
            return ResponseEntity.internalServerError().body(Collections.emptyList());
        }
    }
}
package com.example.demo.controller;

import com.example.demo.dto.ChatRequest; // 아래 DTO 생성 필요
import com.example.demo.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    // POST /api/chatbot/recommend 엔드포인트
    @PostMapping("/recommend")
    public ResponseEntity<String> getMusicRecommendation(@RequestBody ChatRequest chatRequest) {
        try {
            // ChatbotService를 호출하여 AI로부터 추천 받기
            String recommendation = chatbotService.getRecommendation(chatRequest.getPrompt());
            return ResponseEntity.ok(recommendation); // AI 응답 반환
        } catch (Exception e) {
            e.printStackTrace(); // 서버 로그에 에러 출력
            return ResponseEntity.internalServerError().body("AI 추천 생성 중 오류 발생: " + e.getMessage());
        }
    }
}
package com.example.demo.controller;

import com.example.demo.dto.ChatRequest;
import com.example.demo.entity.Song;
import com.example.demo.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// 🚨 @CrossOrigin 제거 (WebConfig의 전역 설정을 따름)

import java.util.List;
import java.util.Collections;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/recommend")
    public ResponseEntity<List<Song>> getMusicRecommendation(@RequestBody ChatRequest chatRequest) {
        try {
            List<Song> recommendation = chatbotService.getRecommendation(chatRequest.getPrompt());
            return ResponseEntity.ok(recommendation);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Collections.emptyList());
        }
    }
}
package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value; // 🚨 @Value 어노테이션 import
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

@Service
public class ChatbotService {

    // 🚨 application.properties에서 API 키를 주입받습니다.
    @Value("${gemini.api.key}")
    private String apiKey;

    // 🚨 API URL (API 키 부분을 동적으로 생성하도록 수정)
    private String getApiUrl() {
        return "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-preview-09-2025:generateContent?key=" + this.apiKey;
    }

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getRecommendation(String userPrompt) throws Exception {
        // 🚨 API 키가 제대로 주입되었는지 확인 (서버 시작 시 오류 방지)
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Gemini API Key가 application.properties에 설정되지 않았습니다.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 시스템 명령어 정의
        String systemInstruction = "당신은 음악 추천 전문가입니다. 사용자의 요청에 따라 5개의 곡을 추천해주세요. 응답 형식은 반드시 '곡 제목 - 아티스트' 형태로, 각 곡을 줄바꿈하여 나열해주세요. 다른 설명은 추가하지 마세요.";

        // 요청 본문 구성
        Map<String, Object> textPartUser = new HashMap<>();
        textPartUser.put("text", userPrompt);
        Map<String, Object> contentUser = new HashMap<>();
        contentUser.put("parts", Collections.singletonList(textPartUser));

        Map<String, Object> textPartSystem = new HashMap<>();
        textPartSystem.put("text", systemInstruction);
        Map<String, Object> contentSystem = new HashMap<>();
        contentSystem.put("parts", Collections.singletonList(textPartSystem));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", Collections.singletonList(contentUser));
        requestBody.put("systemInstruction", contentSystem);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // Gemini API 호출 (getApiUrl() 메서드 사용)
        ResponseEntity<String> response = restTemplate.postForEntity(getApiUrl(), entity, String.class);

        // 응답 처리
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode textNode = root.path("candidates").get(0).path("content").path("parts").get(0).path("text");

             if (textNode.isMissingNode()) {
                 JsonNode errorNode = root.path("error").path("message");
                 if (!errorNode.isMissingNode()) {
                     throw new RuntimeException("Gemini API Error: " + errorNode.asText());
                 }
                 System.err.println("Unexpected API response structure: " + response.getBody());
                 return "AI 응답 파싱 오류: 예상된 텍스트 경로를 찾을 수 없습니다.";
             }
             return textNode.asText("추천을 생성하지 못했습니다.");

        } else {
             throw new RuntimeException("Gemini API 호출 실패: " + response.getStatusCode() + " Body: " + response.getBody());
        }
    }
}
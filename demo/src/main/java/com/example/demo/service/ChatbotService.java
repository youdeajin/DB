package com.example.demo.service;

import com.example.demo.entity.Artist;
import com.example.demo.entity.Song;
import com.example.demo.repository.ArtistRepository;
import com.example.demo.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.net.URI; // 🚨 필수

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    @Value("${gemini.api.key}")
    private String apiKey;

    // 🚨 [정정] 2025년 최신 모델인 'gemini-2.5-flash'로 설정
    private URI getApiUri() {
        String urlString = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + this.apiKey;
        return URI.create(urlString);
    }

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;

    public List<Song> getRecommendation(String userPrompt) throws Exception {
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("❌ API Key가 없습니다.");
            return new ArrayList<>();
        }

        List<Song> allSongs = songRepository.findAll();
        List<Artist> allArtists = artistRepository.findAll();
        
        if (allSongs.isEmpty()) {
            System.out.println("⚠️ DB에 곡 데이터가 없습니다.");
            return new ArrayList<>();
        }

        // 곡 목록 컨텍스트 생성
        String songListContext = allSongs.stream()
            .map(song -> {
                String artistName = allArtists.stream()
                                  .filter(a -> a.getArtistId().equals(song.getArtistId()))
                                  .map(Artist::getName)
                                  .findFirst()
                                  .orElse("Unknown");
                return song.getTitle() + " - " + artistName;
            })
            .limit(100)
            .collect(Collectors.joining("\n"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String systemInstruction = "당신은 음악 추천 DJ입니다. 사용자의 기분이나 요청에 맞춰, **반드시 아래 제공된 [보유 곡 목록] 중에서만** 5곡을 골라 추천해주세요.\n" +
            "목록에 없는 곡은 절대 추천하지 마세요.\n" +
            "응답 형식은 반드시 '곡 제목 - 아티스트' (예: Dynamite - BTS) 형식으로 한 줄에 한 곡씩 작성하세요.\n" +
            "번호(1. 등)나 따옴표, 부가 설명은 절대 붙이지 마세요.\n\n" +
            "--- [보유 곡 목록] 시작 ---\n" +
            songListContext + "\n" +
            "--- [보유 곡 목록] 끝 ---\n\n" +
            "사용자 요청: " + userPrompt;

        Map<String, Object> contentPart = new HashMap<>();
        contentPart.put("text", systemInstruction);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", Collections.singletonList(contentPart));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", Collections.singletonList(content));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        System.out.println("🤖 Gemini API 호출 중... (Model: gemini-2.5-flash)");
        
        try {
            // URI 객체 사용 (404 방지)
            ResponseEntity<String> response = restTemplate.postForEntity(getApiUri(), entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode candidates = root.path("candidates");
                
                if (candidates.isEmpty()) {
                    System.err.println("❌ AI 응답 없음 (Safety Filter 등)");
                    return new ArrayList<>();
                }

                String aiResponseText = candidates.get(0).path("content").path("parts").get(0).path("text").asText();
                System.out.println("✅ AI 응답 수신:\n" + aiResponseText);
                
                return parseAiResponseAndFindSongs(aiResponseText, allSongs, allArtists);
            }
        } catch (Exception e) {
            System.err.println("❌ API 호출 에러: " + e.getMessage());
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private List<Song> parseAiResponseAndFindSongs(String aiResponseText, List<Song> allSongs, List<Artist> allArtists) {
        List<Song> foundSongs = new ArrayList<>();
        String[] lines = aiResponseText.split("\n");

        for (String line : lines) {
            String cleanLine = line.replaceAll("^\\d+\\.\\s*", "").replaceAll("[\"']", "").trim();
            if (cleanLine.isEmpty()) continue;
            
            String[] parts = cleanLine.split(" - ");
            String titleQuery = parts[0].trim();
            
            Optional<Song> matchingSong = allSongs.stream()
                .filter(song -> song.getTitle().toLowerCase().contains(titleQuery.toLowerCase()))
                .findFirst();

            if (matchingSong.isPresent()) {
                foundSongs.add(matchingSong.get());
            } else {
                 List<Song> retry = songRepository.findByTitleContainingIgnoreCase(titleQuery);
                 if (!retry.isEmpty()) foundSongs.add(retry.get(0));
            }
        }
        return foundSongs;
    }
}
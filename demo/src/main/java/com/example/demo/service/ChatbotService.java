package com.example.demo.service;

import com.example.demo.entity.Album;
import com.example.demo.entity.Artist;
import com.example.demo.entity.Song;
import com.example.demo.repository.ArtistRepository;
import com.example.demo.repository.SongRepository;
import lombok.RequiredArgsConstructor; // 🚨 RequiredArgsConstructor 추가
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;
import java.util.List; // 🚨 List import
import java.util.ArrayList; // 🚨 ArrayList import
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors; // 🚨 Collectors import

@Service
@RequiredArgsConstructor // 🚨 final 필드 주입을 위해 @RequiredArgsConstructor 추가
public class ChatbotService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private String getApiUrl() {
        return "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-preview-09-2025:generateContent?key=" + this.apiKey;
    }

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 🚨 [새로 추가] DB 접근을 위한 Repository 주입
    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;

    /**
     * 🚨 [수정] AI가 추천한 "텍스트"가 아닌 "Song 객체 리스트"를 반환하도록 변경
     */
    public List<Song> getRecommendation(String userPrompt) throws Exception {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Gemini API Key가 application.properties에 설정되지 않았습니다.");
        }

        // --- 1. DB에서 모든 곡 목록 가져오기 ---
        List<Song> allSongs = songRepository.findAll();
        List<Artist> allArtists = artistRepository.findAll();
        
        // DB가 비어있으면 AI 호출 없이 빈 리스트 반환
        if (allSongs.isEmpty()) {
            System.out.println("DB에 곡이 없어 추천을 건너뜁니다.");
            return new ArrayList<>();
        }

        // --- 2. AI에게 전달할 '컨텍스트' (DB 곡 목록) 생성 ---
        // 예: "1: LILAC - 아이유\n2: Dynamite - BTS\n..."
        String songListContext = allSongs.stream()
            .map(song -> {
                String artistName = allArtists.stream()
                                  .filter(a -> a.getArtistId().equals(song.getArtistId()))
                                  .map(Artist::getName)
                                  .findFirst()
                                  .orElse("알 수 없는 아티스트");
                // AI가 파싱하기 쉽도록 "제목 - 아티스트" 형식 사용
                return "'" + song.getTitle() + " - " + artistName + "'";
            })
            .collect(Collectors.joining("\n")); // 줄바꿈으로 구분

        // --- 3. Gemini API 호출 ---
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 🚨 [수정] 시스템 지침: DB 목록을 포함시키고, 목록 내에서만 응답하도록 강제
        String systemInstruction = "당신은 음악 추천 전문가입니다. " +
            "사용자의 요청에 가장 잘 맞는 곡 5개를 **반드시 아래 제공된 목록 안에서만** 골라야 합니다. " +
            "목록에 없는 곡은 절대 추천해서는 안 됩니다.\n" +
            "응답 형식은 반드시 '곡 제목 - 아티스트' 형태로, 각 곡을 줄바꿈하여 나열해주세요. 다른 설명은 추가하지 마세요.\n\n" +
            "--- 제공된 곡 목록 시작 ---\n" +
            songListContext + "\n" +
            "--- 제공된 곡 목록 끝 ---";

        // 요청 본문 구성 (기존과 동일)
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

        // Gemini API 호출
        ResponseEntity<String> response = restTemplate.postForEntity(getApiUrl(), entity, String.class);

        // --- 4. AI 응답(텍스트) 파싱 및 DB 재검색 ---
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode textNode = root.path("candidates").get(0).path("content").path("parts").get(0).path("text");
            
            if (textNode.isMissingNode()) {
                 // ... (기존 오류 처리)
                 throw new RuntimeException("AI 응답 파싱 오류");
            }
            
            String aiResponseText = textNode.asText();
            
            // 5. AI 응답(텍스트)을 기반으로 DB에서 Song 객체 리스트 재구성
            return parseAiResponseAndFindSongs(aiResponseText, allSongs, allArtists);

        } else {
             throw new RuntimeException("Gemini API 호출 실패: " + response.getStatusCode());
        }
    }

    /**
     * 🚨 [새로 추가] AI의 텍스트 응답("제목 - 아티스트")을 파싱하고,
     * 미리 로드된 allSongs 리스트에서 일치하는 Song 객체를 찾는 헬퍼 메서드
     */
    private List<Song> parseAiResponseAndFindSongs(String aiResponseText, List<Song> allSongs, List<Artist> allArtists) {
        List<Song> foundSongs = new ArrayList<>();
        String[] lines = aiResponseText.split("\n"); // 줄바꿈으로 분리

        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            
            String[] parts = line.split(" - "); // "제목 - 아티스트" 분리
            if (parts.length < 2) continue; // 형식이 안 맞으면 건너뛰기
            
            String title = parts[0].trim();
            String artistName = parts[1].trim();

            // 미리 로드된 allSongs 리스트에서 일치하는 곡 검색
            Optional<Song> matchingSong = allSongs.stream()
                .filter(song -> {
                    // DB에서 가져온 아티스트 이름
                    String dbArtistName = allArtists.stream()
                                    .filter(a -> a.getArtistId().equals(song.getArtistId()))
                                    .map(Artist::getName)
                                    .findFirst().orElse("");
                    // 제목과 아티스트 이름이 모두 일치하는지 확인
                    return song.getTitle().equalsIgnoreCase(title) && dbArtistName.equalsIgnoreCase(artistName);
                })
                .findFirst();

            if (matchingSong.isPresent()) {
                foundSongs.add(matchingSong.get());
            } else {
                 // AI가 목록에 없는 곡을 추천했거나 파싱 오류일 수 있음 (로그만 남김)
                 System.err.println("AI 추천 곡을 DB에서 찾지 못했습니다: " + line);
                 // 🚨 대안: 제목으로만이라도 다시 검색 (songRepository.findByTitle(title))
                 List<Song> foundByTitle = songRepository.findByTitle(title);
                 if (!foundByTitle.isEmpty()) {
                     foundSongs.add(foundByTitle.get(0)); // 제목이 일치하는 첫 번째 곡 추가
                 }
            }
        }
        return foundSongs;
    }
}
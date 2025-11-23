package com.example.demo.service;

// 🚨 사용하지 않는 Import 제거 (Album)
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

    private String getApiUrl() {
        return "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-preview-09-2025:generateContent?key=" + this.apiKey;
    }

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;

    public List<Song> getRecommendation(String userPrompt) throws Exception {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Gemini API Key가 application.properties에 설정되지 않았습니다.");
        }

        List<Song> allSongs = songRepository.findAll();
        List<Artist> allArtists = artistRepository.findAll();
        
        if (allSongs.isEmpty()) {
            System.out.println("DB에 곡이 없어 추천을 건너뜁니다.");
            return new ArrayList<>();
        }

        String songListContext = allSongs.stream()
            .map(song -> {
                String artistName = allArtists.stream()
                                  .filter(a -> a.getArtistId().equals(song.getArtistId()))
                                  .map(Artist::getName)
                                  .findFirst()
                                  .orElse("알 수 없는 아티스트");
                return "'" + song.getTitle() + " - " + artistName + "'";
            })
            .collect(Collectors.joining("\n"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String systemInstruction = "당신은 음악 추천 전문가입니다. " +
            "사용자의 요청에 가장 잘 맞는 곡 5개를 **반드시 아래 제공된 목록 안에서만** 골라야 합니다. " +
            "목록에 없는 곡은 절대 추천해서는 안 됩니다.\n" +
            "응답 형식은 반드시 '곡 제목 - 아티스트' 형태로, 각 곡을 줄바꿈하여 나열해주세요. 다른 설명은 추가하지 마세요.\n\n" +
            "--- 제공된 곡 목록 시작 ---\n" +
            songListContext + "\n" +
            "--- 제공된 곡 목록 끝 ---";

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

        ResponseEntity<String> response = restTemplate.postForEntity(getApiUrl(), entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode textNode = root.path("candidates").get(0).path("content").path("parts").get(0).path("text");
            
            if (textNode.isMissingNode()) {
                 throw new RuntimeException("AI 응답 파싱 오류");
            }
            
            String aiResponseText = textNode.asText();
            
            return parseAiResponseAndFindSongs(aiResponseText, allSongs, allArtists);

        } else {
             throw new RuntimeException("Gemini API 호출 실패: " + response.getStatusCode());
        }
    }

    private List<Song> parseAiResponseAndFindSongs(String aiResponseText, List<Song> allSongs, List<Artist> allArtists) {
        List<Song> foundSongs = new ArrayList<>();
        String[] lines = aiResponseText.split("\n");

        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            
            String[] parts = line.split(" - ");
            if (parts.length < 2) continue;
            
            String title = parts[0].trim();
            String artistName = parts[1].trim();

            Optional<Song> matchingSong = allSongs.stream()
                .filter(song -> {
                    String dbArtistName = allArtists.stream()
                                    .filter(a -> a.getArtistId().equals(song.getArtistId()))
                                    .map(Artist::getName)
                                    .findFirst().orElse("");
                    return song.getTitle().equalsIgnoreCase(title) && dbArtistName.equalsIgnoreCase(artistName);
                })
                .findFirst();

            if (matchingSong.isPresent()) {
                foundSongs.add(matchingSong.get());
            } else {
                 System.err.println("AI 추천 곡을 DB에서 찾지 못했습니다: " + line);
                 List<Song> foundByTitle = songRepository.findByTitle(title);
                 if (!foundByTitle.isEmpty()) {
                     foundSongs.add(foundByTitle.get(0));
                 }
            }
        }
        return foundSongs;
    }
}
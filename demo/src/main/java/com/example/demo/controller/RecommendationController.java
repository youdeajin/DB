package com.example.demo.controller;

import com.example.demo.entity.Song;
import com.example.demo.repository.SongRepository;
import com.example.demo.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final WeatherService weatherService;
    private final SongRepository songRepository;

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/weather")
    public ResponseEntity<?> getWeatherRecommendation() {
        // 1. 현재 날씨 가져오기
        String weather = weatherService.getCurrentWeather();

        // 2. 날씨에 따른 장르 키워드 설정
        List<String> targetGenres = new ArrayList<>();
        String message = "";

        switch (weather) {
            case "Rainy":
                message = "비 오는 날엔 감성적인 발라드와 R&B 어떠세요? ☔";
                targetGenres.add("Ballad");
                targetGenres.add("R&B");
                targetGenres.add("Jazz");
                break;
            case "Snowy":
                message = "눈 오는 날, 따뜻한 노래를 들어보세요 ❄️";
                targetGenres.add("Ballad");
                targetGenres.add("R&B");
                targetGenres.add("Carol");
                break;
            default: // Sunny
                message = "맑은 날씨! 신나는 음악으로 기분을 업해보세요! ☀️";
                targetGenres.add("Dance");
                targetGenres.add("Pop");
                targetGenres.add("Rock");
                targetGenres.add("Hip-hop");
                break;
        }

        // 3. DB에서 해당 장르의 곡들 찾기 (최대 8개)
        List<Song> allSongs = songRepository.findAll();
        List<Song> recommendedSongs = allSongs.stream()
                .filter(song -> {
                    if (song.getGenre() == null) return false;
                    for (String genre : targetGenres) {
                        if (song.getGenre().toLowerCase().contains(genre.toLowerCase())) {
                            return true;
                        }
                    }
                    return false;
                })
                .limit(16) // 일단 매칭되는 것 최대 8개 가져옴
                .collect(Collectors.toList());
        
        // 🚨 [수정된 부분] 8개가 안 되면 나머지를 랜덤으로 채우기!
        int targetCount = 16;
        if (recommendedSongs.size() < targetCount) {
            // 부족한 개수 계산
            int needed = targetCount - recommendedSongs.size();
            
            // 부족한 만큼 랜덤 곡 가져오기
            List<Song> randomSongs = songRepository.findRandomSongs(needed);
            
            // 중복 방지하며 추가
            for (Song randomSong : randomSongs) {
                // 이미 추천 목록에 없는 곡만 추가
                boolean exists = recommendedSongs.stream()
                    .anyMatch(s -> s.getSongId().equals(randomSong.getSongId()));
                
                if (!exists) {
                    recommendedSongs.add(randomSong);
                }
            }
            
            // 메시지에 안내 문구 추가 (너무 적을 때만)
            if (!message.contains("랜덤")) { // 중복 추가 방지
                 message += " (비슷한 분위기의 곡과 추천곡을 섞어봤어요!)";
            }
        }

        // 4. 결과 반환
        Map<String, Object> response = new HashMap<>();
        response.put("weather", weather);
        response.put("message", message);
        response.put("songs", recommendedSongs);

        return ResponseEntity.ok(response);
    }
}
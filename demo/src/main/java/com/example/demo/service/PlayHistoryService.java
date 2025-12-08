package com.example.demo.service;

import com.example.demo.entity.PlayHistory;
import com.example.demo.entity.Song;
import com.example.demo.repository.PlayHistoryRepository;
import com.example.demo.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlayHistoryService {

    private final PlayHistoryRepository playHistoryRepository;
    private final SongRepository songRepository;

    /**
     * 재생 기록 저장
     */
    @Transactional
    public PlayHistory recordPlay(Long userId, Long songId) {
        PlayHistory playHistory = new PlayHistory(userId, songId, LocalDateTime.now());
        return playHistoryRepository.save(playHistory);
    }

    /**
     * 특정 사용자의 최근 재생 기록 조회 (최신순)
     */
    @Transactional(readOnly = true)
    public List<PlayHistory> getRecentPlayHistory(Long userId) {
        return playHistoryRepository.findByUserIdOrderByPlayedAtDesc(userId);
    }

    /**
     * 특정 사용자의 최근 재생 기록 조회 (제한된 개수)
     */
    @Transactional(readOnly = true)
    public List<PlayHistory> getRecentPlayHistory(Long userId, int limit) {
        return playHistoryRepository.findRecentByUserId(userId, limit);
    }

    /**
     * 특정 사용자의 최근 재생 곡 목록 조회 (Song 엔티티 포함)
     */
    @Transactional(readOnly = true)
    public List<Song> getRecentSongs(Long userId, int limit) {
        List<PlayHistory> historyList = playHistoryRepository.findRecentByUserId(userId, limit);
        
        if (historyList.isEmpty()) {
            return List.of();
        }
        
        // 중복 제거 (같은 곡이 여러 번 재생된 경우 최신 것만 유지)
        // 순서를 유지하기 위해 LinkedHashSet 사용
        List<Long> uniqueSongIds = historyList.stream()
            .map(PlayHistory::getSongId)
            .distinct()
            .collect(Collectors.toList());

        // Song을 ID 순서대로 매핑
        List<Song> allSongs = songRepository.findAllById(uniqueSongIds);
        
        // 재생 순서 유지를 위한 맵 생성
        Map<Long, Song> songMap = allSongs.stream()
            .collect(Collectors.toMap(Song::getSongId, song -> song));
        
        // 재생 순서대로 Song 반환
        return uniqueSongIds.stream()
            .map(songMap::get)
            .filter(song -> song != null)
            .collect(Collectors.toList());
    }
}


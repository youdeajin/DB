package com.example.demo.service;

import com.example.demo.dto.PlaylistRequest;
import com.example.demo.dto.PlaylistTitleUpdateRequest;
import com.example.demo.entity.Playlist;
import com.example.demo.entity.PlaylistSong;
import com.example.demo.repository.PlaylistRepository;
import com.example.demo.repository.PlaylistSongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.PlaylistDetailResponse; // 🚨 DTO import
import com.example.demo.entity.Song; // 🚨 Song import
import com.example.demo.repository.SongRepository; // 🚨 SongRepository import
import java.util.Collections;
import java.util.stream.Collectors;

import java.time.LocalDateTime;
import java.util.List; // 🚨 List import

import com.example.demo.dto.PlaylistTitleUpdateRequest;

import com.example.demo.dto.PlaylistSongsUpdateRequest;

@Service
@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistSongRepository playlistSongRepository;
    private final SongRepository songRepository;

    // 재생목록 생성
    @Transactional
    public Playlist createPlaylist(PlaylistRequest request, Long userId) {
        // ... (생성 로직) ...
        Playlist playlist = new Playlist(
                request.getTitle(),
                userId, 
                request.getIsPublic() ? 1 : 0, 
                LocalDateTime.now()
        );
        playlist = playlistRepository.save(playlist);

        if (request.getSongIds() != null && !request.getSongIds().isEmpty()) {
            int order = 1;
            for (Long songId : request.getSongIds()) {
                PlaylistSong playlistSong = new PlaylistSong(
                        playlist.getPlaylistId(),
                        songId,
                        order++
                );
                playlistSongRepository.save(playlistSong);
            }
        }
        
        return playlist;
    }

    // 사용자 재생목록 조회
    @Transactional(readOnly = true)
    public List<Playlist> findUserPlaylists(Long userId) {
        // Repository 메서드 호출
        return playlistRepository.findByOwnerUserId(userId); 
    }
    
    @Transactional(readOnly = true)
    public List<Playlist> findAllPlaylists() {
        return playlistRepository.findAll(); // JpaRepository의 기본 메서드 사용
    }

    /**
     * 🚨 [새로 추가] 특정 재생목록의 상세 정보(곡 목록 포함)를 조회합니다.
     * @param playlistId 조회할 재생목록 ID
     * @return PlaylistDetailResponse DTO
     */
    @Transactional(readOnly = true)
    public PlaylistDetailResponse findPlaylistById(Long playlistId) {
        // 1. 재생목록 정보 조회
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("Playlist not found with ID: " + playlistId));

        // 2. 해당 재생목록의 곡 ID 및 순서 목록 조회 (순서대로)
        List<PlaylistSong> playlistSongs = playlistSongRepository.findByPlaylistIdOrderBySongOrderAsc(playlistId);

        // 3. 곡 ID 목록 추출
        List<Long> songIds = playlistSongs.stream()
                                          .map(PlaylistSong::getSongId)
                                          .collect(Collectors.toList());

        // 4. 곡 ID 목록으로 실제 곡 정보 조회 (ID 순서대로 반환될 수 있으므로 정렬 필요)
        List<Song> songs = songRepository.findAllById(songIds);

        // 5. 원래 순서(songOrder)대로 곡 목록 정렬 (선택적이지만 권장)
        // findAllById는 ID 순서대로 반환하므로, PlaylistSong의 순서대로 재정렬
        List<Song> sortedSongs = songIds.stream()
            .map(id -> songs.stream().filter(s -> s.getSongId().equals(id)).findFirst().orElse(null))
            .filter(song -> song != null) // 혹시 모를 null 제거
            .collect(Collectors.toList());


        // 6. DTO 생성 및 반환
        return new PlaylistDetailResponse(playlist, sortedSongs);
    }
    /**
     * 🚨 [새로 추가] 특정 재생목록을 삭제합니다.
     * 재생목록에 포함된 곡 정보(PLAYLIST_SONGS)도 함께 삭제합니다.
     * @param playlistId 삭제할 재생목록 ID
     */
    @Transactional
    public void deletePlaylist(Long playlistId) {
        // 1. 해당 재생목록이 존재하는지 확인 (없으면 예외 발생)
        if (!playlistRepository.existsById(playlistId)) {
            throw new IllegalArgumentException("Playlist not found with ID: " + playlistId);
        }

        // 2. 해당 재생목록에 속한 모든 곡 정보(PlaylistSong) 삭제
        // playlistId로 PlaylistSong 목록을 찾아서 삭제하는 것이 더 안전하지만,
        // 여기서는 간단하게 JPQL 또는 deleteAllByPlaylistId (Repository에 추가) 사용 가능
        // 여기서는 JPQL이나 deleteAllByPlaylistId 대신, 해당 ID를 가진 엔티티를 찾아서 삭제합니다.
        List<PlaylistSong> songsToDelete = playlistSongRepository.findByPlaylistIdOrderBySongOrderAsc(playlistId);
        playlistSongRepository.deleteAll(songsToDelete); // 찾은 엔티티 목록 삭제

        // 3. 재생목록 본체(Playlist) 삭제
        playlistRepository.deleteById(playlistId);
    }

    /**
     * 특정 재생목록의 제목을 수정
     * @param playlistId 수정할 재생목록 ID
     * @param request 새 제목 정보 DTO
     * @return 수정된 Playlist Entity
     */
    @Transactional
    public Playlist updatePlaylistTitle(Long playlistId, PlaylistTitleUpdateRequest request) {
        // 1. 해당 ID의 재생목록을 찾습니다. 없으면 예외 발생.
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("Playlist not found with ID: " + playlistId));

        // 2. 엔티티의 제목을 새로운 제목으로 업데이트합니다.
        // Playlist 엔티티에 setTitle 메서드가 필요합니다. (아래 엔티티 수정 참고)
        playlist.setTitle(request.getNewTitle()); 

        // 3. 변경된 엔티티를 저장합니다. (JPA가 변경 감지하여 UPDATE 쿼리 실행)
        // save()를 명시적으로 호출할 수도 있습니다.
        playlistRepository.save(playlist); 

        return playlist;
    }
    /**
     * 🚨 [새로 추가] 특정 재생목록의 곡 목록을 업데이트합니다.
     * 기존 곡 목록을 모두 삭제하고 새로운 목록으로 덮어씁니다.
     * @param playlistId 수정할 재생목록 ID
     * @param request 새 곡 ID 목록 DTO
     */
    @Transactional
    public void updatePlaylistSongs(Long playlistId, PlaylistSongsUpdateRequest request) {
        // 1. 해당 재생목록이 존재하는지 확인
        if (!playlistRepository.existsById(playlistId)) {
            throw new IllegalArgumentException("Playlist not found with ID: " + playlistId);
        }

        // 2. 기존 곡 목록 모두 삭제 (JPQL 사용)
        playlistSongRepository.deleteAllByPlaylistId(playlistId);

        // 3. 새로운 곡 목록 추가
        if (request.getSongIds() != null && !request.getSongIds().isEmpty()) {
            int order = 1;
            for (Long songId : request.getSongIds()) {
                // Song 엔티티 존재 여부 확인 (선택 사항이지만 권장)
                if (!songRepository.existsById(songId)) {
                   System.out.println("Warning: Song with ID " + songId + " not found, skipping.");
                   continue; // 존재하지 않는 곡 ID는 건너뜁니다.
                }

                PlaylistSong playlistSong = new PlaylistSong(
                        playlistId,
                        songId,
                        order++
                );
                playlistSongRepository.save(playlistSong);
            }
        }
    }
    // PlaylistService.java 파일에 추가

    /**
     * 특정 재생목록에 곡 하나를 추가합니다.
     * @param playlistId 곡을 추가할 재생목록 ID
     * @param songId 추가할 곡 ID
     * @throws IllegalArgumentException 재생목록 또는 곡이 없을 경우, 이미 곡이 포함된 경우
     */
    @Transactional
    public void addSongToPlaylist(Long playlistId, Long songId) {
        // 1. 재생목록 및 곡 존재 여부 확인
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("Playlist not found with ID: " + playlistId));
        if (!songRepository.existsById(songId)) {
            throw new IllegalArgumentException("Song not found with ID: " + songId);
        }

        // 2. 이미 해당 곡이 재생목록에 있는지 확인 (선택 사항)
        List<PlaylistSong> existingSongs = playlistSongRepository.findByPlaylistIdOrderBySongOrderAsc(playlistId);
        boolean alreadyExists = existingSongs.stream().anyMatch(ps -> ps.getSongId().equals(songId));
        if (alreadyExists) {
             System.out.println("Song " + songId + " already exists in playlist " + playlistId);
             // 예외를 던지거나 그냥 종료할 수 있습니다. 여기서는 그냥 종료.
             return;
             // throw new IllegalArgumentException("Song already exists in this playlist.");
        }

        // 3. 마지막 순서(order) 계산
        int nextOrder = existingSongs.size() + 1;

        // 4. PlaylistSong 엔티티 생성 및 저장
        PlaylistSong newPlaylistSong = new PlaylistSong(playlistId, songId, nextOrder);
        playlistSongRepository.save(newPlaylistSong);

        System.out.println("Song " + songId + " added to playlist " + playlistId + " at order " + nextOrder);
    }
}


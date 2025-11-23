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

import com.example.demo.dto.PlaylistDetailResponse;
import com.example.demo.entity.Song;
import com.example.demo.repository.SongRepository;
// 🚨 사용하지 않는 Import 제거 (java.util.Collections 등)
import java.util.stream.Collectors;

import java.time.LocalDateTime;
import java.util.List;

import com.example.demo.dto.PlaylistSongsUpdateRequest;

@Service
@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistSongRepository playlistSongRepository;
    private final SongRepository songRepository;

    @Transactional
    public Playlist createPlaylist(PlaylistRequest request, Long userId) {
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

    @Transactional(readOnly = true)
    public List<Playlist> findUserPlaylists(Long userId) {
        return playlistRepository.findByOwnerUserId(userId); 
    }
    
    @Transactional(readOnly = true)
    public List<Playlist> findAllPlaylists() {
        return playlistRepository.findAll();
    }

    @Transactional(readOnly = true)
    public PlaylistDetailResponse findPlaylistById(Long playlistId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("Playlist not found with ID: " + playlistId));

        List<PlaylistSong> playlistSongs = playlistSongRepository.findByPlaylistIdOrderBySongOrderAsc(playlistId);

        List<Long> songIds = playlistSongs.stream()
                                          .map(PlaylistSong::getSongId)
                                          .collect(Collectors.toList());

        List<Song> songs = songRepository.findAllById(songIds);

        List<Song> sortedSongs = songIds.stream()
            .map(id -> songs.stream().filter(s -> s.getSongId().equals(id)).findFirst().orElse(null))
            .filter(song -> song != null)
            .collect(Collectors.toList());

        return new PlaylistDetailResponse(playlist, sortedSongs);
    }

    @Transactional
    public void deletePlaylist(Long playlistId) {
        if (!playlistRepository.existsById(playlistId)) {
            throw new IllegalArgumentException("Playlist not found with ID: " + playlistId);
        }
        List<PlaylistSong> songsToDelete = playlistSongRepository.findByPlaylistIdOrderBySongOrderAsc(playlistId);
        playlistSongRepository.deleteAll(songsToDelete);
        playlistRepository.deleteById(playlistId);
    }

    @Transactional
    public Playlist updatePlaylistTitle(Long playlistId, PlaylistTitleUpdateRequest request) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("Playlist not found with ID: " + playlistId));

        playlist.setTitle(request.getNewTitle()); 
        // 🚨 playlist 변수 미사용 경고 해결을 위해 반환값 활용
        return playlistRepository.save(playlist); 
    }

    @Transactional
    public void updatePlaylistSongs(Long playlistId, PlaylistSongsUpdateRequest request) {
        if (!playlistRepository.existsById(playlistId)) {
            throw new IllegalArgumentException("Playlist not found with ID: " + playlistId);
        }

        playlistSongRepository.deleteAllByPlaylistId(playlistId);

        if (request.getSongIds() != null && !request.getSongIds().isEmpty()) {
            int order = 1;
            for (Long songId : request.getSongIds()) {
                if (!songRepository.existsById(songId)) {
                   System.out.println("Warning: Song with ID " + songId + " not found, skipping.");
                   continue;
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

    @Transactional
    public void addSongToPlaylist(Long playlistId, Long songId) {
        // playlist 존재 확인 (ID 사용)
        if (!playlistRepository.existsById(playlistId)) {
             throw new IllegalArgumentException("Playlist not found with ID: " + playlistId);
        }
        if (!songRepository.existsById(songId)) {
            throw new IllegalArgumentException("Song not found with ID: " + songId);
        }

        List<PlaylistSong> existingSongs = playlistSongRepository.findByPlaylistIdOrderBySongOrderAsc(playlistId);
        boolean alreadyExists = existingSongs.stream().anyMatch(ps -> ps.getSongId().equals(songId));
        if (alreadyExists) {
             System.out.println("Song " + songId + " already exists in playlist " + playlistId);
             return;
        }

        int nextOrder = existingSongs.size() + 1;
        PlaylistSong newPlaylistSong = new PlaylistSong(playlistId, songId, nextOrder);
        playlistSongRepository.save(newPlaylistSong);

        System.out.println("Song " + songId + " added to playlist " + playlistId + " at order " + nextOrder);
    }

    @Transactional
    public void removeSongFromPlaylist(Long playlistId, Long songId) {
        playlistSongRepository.deleteByPlaylistIdAndSongId(playlistId, songId);
    }
}
package com.example.demo.service;

import com.example.demo.entity.Artist;
import com.example.demo.entity.Album;
import com.example.demo.entity.Song;
import com.example.demo.repository.ArtistRepository;
import com.example.demo.repository.AlbumRepository;
import com.example.demo.repository.SongRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchTracksRequest;

import jakarta.annotation.PostConstruct;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
// 🚨 사용하지 않는 Import 제거 (java.util.Optional)

@Service
@RequiredArgsConstructor
public class SpotifyService {

    @Value("${spotify.client.id}")
    private String clientId;

    @Value("${spotify.client.secret}")
    private String clientSecret;

    private SpotifyApi spotifyApi;

    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final SongRepository songRepository;

    @PostConstruct
    public void initialize() {
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .build();
        refreshAccessToken();
    }

    private void refreshAccessToken() {
        if (clientId == null || clientId.isEmpty() || clientSecret == null || clientSecret.isEmpty()) {
             System.err.println("Spotify Client ID 또는 Secret이 application.properties에 설정되지 않았습니다.");
             return;
        }
        try {
            ClientCredentialsRequest credentialsRequest = spotifyApi.clientCredentials().build();
            ClientCredentials credentials = credentialsRequest.execute();
            spotifyApi.setAccessToken(credentials.getAccessToken());
            System.out.println("Spotify Access Token 갱신 완료. 만료 시간(초): " + credentials.getExpiresIn());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.err.println("Spotify Access Token 갱신 오류: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Spotify API 초기화 중 예상치 못한 오류 발생: " + e.getMessage());
        }
    }

    @Transactional
    public List<Song> searchAndSaveTracks(String query, int limit) {
        List<Song> savedSongs = new ArrayList<>();

        if (spotifyApi == null || spotifyApi.getAccessToken() == null) {
            System.err.println("Spotify API가 초기화되지 않았거나 Access Token이 없습니다.");
            return savedSongs;
        }

        try {
            SearchTracksRequest searchRequest = spotifyApi.searchTracks(query)
                    .limit(limit)
                    .build();

            Paging<Track> trackPaging = searchRequest.execute();

            if (trackPaging != null && trackPaging.getItems() != null) {
                System.out.println("Spotify에서 " + trackPaging.getItems().length + "개의 트랙 검색 결과 받음.");
                for (Track track : trackPaging.getItems()) {

                    ArtistSimplified primaryArtist = track.getArtists()[0];
                    Artist artistEntity = artistRepository.findByName(primaryArtist.getName())
                            .orElseGet(() -> {
                                System.out.println("새 아티스트 저장: " + primaryArtist.getName());
                                return artistRepository.save(new Artist(primaryArtist.getName()));
                             });

                    AlbumSimplified spotifyAlbum = track.getAlbum();
                    LocalDate releaseDate = parseSpotifyDate(spotifyAlbum.getReleaseDate());

                    String coverUrl = null;
                    Image[] images = spotifyAlbum.getImages();
                    if (images != null && images.length > 0) {
                        coverUrl = images[0].getUrl();
                        System.out.println("앨범 커버 URL 발견: " + spotifyAlbum.getName() + " -> " + coverUrl);
                    } else {
                        System.out.println("앨범 커버 URL 없음: " + spotifyAlbum.getName());
                    }

                    final String finalCoverUrl = coverUrl;
                    Album albumEntity = albumRepository.findByTitleAndArtistId(spotifyAlbum.getName(), artistEntity.getArtistId())
                             .orElseGet(() -> {
                                 System.out.println("새 앨범 저장: " + spotifyAlbum.getName());
                                 return albumRepository.save(new Album(spotifyAlbum.getName(), artistEntity.getArtistId(), releaseDate, finalCoverUrl));
                              });

                    if (albumEntity.getCoverUrl() == null && finalCoverUrl != null) {
                        albumEntity.setCoverUrl(finalCoverUrl);
                        albumRepository.save(albumEntity);
                        System.out.println("기존 앨범 커버 URL 업데이트: " + albumEntity.getTitle());
                    }

                    String trackTitle = track.getName();
                    if (!songRepository.existsByTitleAndArtistId(trackTitle, artistEntity.getArtistId())) {
                         String fixedFilePath = "https://archive.org/download/pkmn-dppt-soundtrack/Disc%201/05%20-%20Twinleaf%20Town%20%28Day%29.mp3";

                         Song songEntity = new Song(
                                 trackTitle,
                                 artistEntity.getArtistId(),
                                 albumEntity.getAlbumId(),
                                 fixedFilePath,
                                 Integer.valueOf(track.getDurationMs() / 1000),
                                 null
                         );
                         savedSongs.add(songRepository.save(songEntity));
                         System.out.println("DB 저장 완료: " + trackTitle + " (filePath: " + fixedFilePath + ")");
                    } else {
                        System.out.println("이미 DB에 존재: " + trackTitle + " - " + artistEntity.getName());
                    }
                }
            } else {
                 System.out.println("Spotify 검색 결과 없음: " + query);
            }
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.err.println("Spotify 트랙 검색 오류: " + e.getMessage());
            e.printStackTrace();

             if (e instanceof SpotifyWebApiException && ((SpotifyWebApiException) e).getMessage() != null &&
                (((SpotifyWebApiException) e).getMessage().contains("expired") || ((SpotifyWebApiException) e).getMessage().contains("invalid access token"))) {
                 System.out.println("Access Token 만료 감지. 갱신 시도...");
                 refreshAccessToken();
             }
        } catch (Exception e) {
            System.err.println("곡 검색 및 저장 중 예상치 못한 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
        return savedSongs;
    }

    private LocalDate parseSpotifyDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) return null;
        try {
            if (dateString.length() == 4) {
                return LocalDate.parse(dateString + "-01-01", DateTimeFormatter.ISO_LOCAL_DATE);
            } else if (dateString.length() == 7) {
                return LocalDate.parse(dateString + "-01", DateTimeFormatter.ISO_LOCAL_DATE);
            } else {
                return LocalDate.parse(dateString.substring(0, 10), DateTimeFormatter.ISO_LOCAL_DATE);
            }
        } catch (DateTimeParseException e) {
            System.err.println("날짜 문자열 파싱 불가: " + dateString + " - " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("날짜 파싱 중 예외 발생: " + dateString + " - " + e.getMessage());
            return null;
        }
    }
}
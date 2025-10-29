package com.example.demo.service;

import com.example.demo.entity.Artist; // Artist 엔티티
import com.example.demo.entity.Album;  // Album 엔티티
import com.example.demo.entity.Song;   // Song 엔티티
import com.example.demo.repository.ArtistRepository; // 관련 Repository 주입
import com.example.demo.repository.AlbumRepository;
import com.example.demo.repository.SongRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Spotify 라이브러리 import
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.*; // Track, ArtistSimplified, AlbumSimplified, Image 등 포함
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchTracksRequest;

import jakarta.annotation.PostConstruct; // Spring 초기화 어노테이션
import org.apache.hc.core5.http.ParseException; // Spotify 라이브러리 예외

import java.io.IOException; // 입출력 예외
import java.time.LocalDate; // 날짜 처리
import java.time.format.DateTimeFormatter; // 날짜 포맷
import java.time.format.DateTimeParseException; // 날짜 파싱 예외
import java.util.ArrayList;
import java.util.List;
import java.util.Optional; // Optional 사용

@Service
@RequiredArgsConstructor // final 필드 생성자 자동 생성
public class SpotifyService {

    // application.properties에서 Spotify 자격 증명 주입
    @Value("${spotify.client.id}")
    private String clientId;

    @Value("${spotify.client.secret}")
    private String clientSecret;

    // Spotify API 메인 객체
    private SpotifyApi spotifyApi;

    // DB 저장을 위한 Repository 주입
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final SongRepository songRepository;

    // Spring Bean 생성 후 초기화 메서드
    @PostConstruct
    public void initialize() {
        // SpotifyApi 객체 빌드
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .build();
        // 애플리케이션 시작 시 Access Token 획득
        refreshAccessToken();
    }

    // Spotify API Access Token 갱신 메서드
    private void refreshAccessToken() {
        // API 키가 로드되지 않았으면 중단
        if (clientId == null || clientId.isEmpty() || clientSecret == null || clientSecret.isEmpty()) {
             System.err.println("Spotify Client ID 또는 Secret이 application.properties에 설정되지 않았습니다.");
             return;
        }
        try {
            // Client Credentials Flow를 사용하여 Access Token 요청
            ClientCredentialsRequest credentialsRequest = spotifyApi.clientCredentials().build();
            ClientCredentials credentials = credentialsRequest.execute();
            // 획득한 토큰을 SpotifyApi 객체에 설정
            spotifyApi.setAccessToken(credentials.getAccessToken());
            System.out.println("Spotify Access Token 갱신 완료. 만료 시간(초): " + credentials.getExpiresIn());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            // 토큰 갱신 중 오류 발생 시 로그 출력
            System.err.println("Spotify Access Token 갱신 오류: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Spotify API 초기화 중 예상치 못한 오류 발생: " + e.getMessage());
        }
    }

    /**
     * Spotify에서 곡을 검색하고, 결과를 분석하여 Artist, Album, Song 정보를
     * 로컬 데이터베이스에 (중복되지 않게) 저장합니다. filePath는 고정된 URL을 사용하고, 앨범 커버 URL을 저장합니다.
     * @param query 검색어 (예: "아이유")
     * @param limit 검색 결과 개수 제한
     * @return DB에 새로 저장된 Song 엔티티 목록
     */
    @Transactional // 여러 DB 작업을 하나의 트랜잭션으로 묶음
    public List<Song> searchAndSaveTracks(String query, int limit) {
        List<Song> savedSongs = new ArrayList<>(); // 새로 저장된 곡 목록

        // API 객체가 초기화되지 않았으면 빈 목록 반환
        if (spotifyApi == null || spotifyApi.getAccessToken() == null) {
            System.err.println("Spotify API가 초기화되지 않았거나 Access Token이 없습니다.");
            return savedSongs;
        }

        try {
            // Spotify API 트랙 검색 요청 생성
            SearchTracksRequest searchRequest = spotifyApi.searchTracks(query)
                    .limit(limit) // 결과 개수 제한
                    .build();

            // API 요청 실행 및 결과 받기
            Paging<Track> trackPaging = searchRequest.execute();

            // 결과가 존재하고, 곡 목록(items)이 null이 아닌 경우 처리
            if (trackPaging != null && trackPaging.getItems() != null) {
                System.out.println("Spotify에서 " + trackPaging.getItems().length + "개의 트랙 검색 결과 받음.");
                for (Track track : trackPaging.getItems()) {

                    // 미리듣기 URL 확인 로직은 제거됨

                    // 1. 아티스트 정보 처리
                    ArtistSimplified primaryArtist = track.getArtists()[0]; // 대표 아티스트 정보
                    // DB에서 해당 이름의 아티스트 조회, 없으면 새로 생성하여 저장
                    Artist artistEntity = artistRepository.findByName(primaryArtist.getName())
                            .orElseGet(() -> {
                                System.out.println("새 아티스트 저장: " + primaryArtist.getName());
                                // Artist 엔티티 생성자 확인
                                return artistRepository.save(new Artist(primaryArtist.getName()));
                             });

                    // --- 2. 앨범 정보 처리 (커버 URL 포함) ---
                    AlbumSimplified spotifyAlbum = track.getAlbum(); // 앨범 정보
                    LocalDate releaseDate = parseSpotifyDate(spotifyAlbum.getReleaseDate()); // 날짜 파싱

                    // 앨범 커버 이미지 URL 추출 (첫 번째 이미지 URL 사용, 없으면 null)
                    String coverUrl = null;
                    Image[] images = spotifyAlbum.getImages(); // 이미지 배열 가져오기
                    if (images != null && images.length > 0) {
                        coverUrl = images[0].getUrl(); // 첫 번째(가장 큰 해상도) 이미지 URL 사용
                        System.out.println("앨범 커버 URL 발견: " + spotifyAlbum.getName() + " -> " + coverUrl);
                    } else {
                        System.out.println("앨범 커버 URL 없음: " + spotifyAlbum.getName());
                    }

                    // DB에서 앨범 조회 또는 새로 생성 (coverUrl 포함)
                    final String finalCoverUrl = coverUrl; // lambda에서 사용하기 위해 final 변수로 선언
                    Album albumEntity = albumRepository.findByTitleAndArtistId(spotifyAlbum.getName(), artistEntity.getArtistId())
                             .orElseGet(() -> {
                                 System.out.println("새 앨범 저장: " + spotifyAlbum.getName());
                                 // 생성자에 coverUrl 전달 (Album 엔티티 생성자 확인 필요)
                                 return albumRepository.save(new Album(spotifyAlbum.getName(), artistEntity.getArtistId(), releaseDate, finalCoverUrl));
                              });

                    // 만약 기존 DB 앨범에 커버 URL이 없었는데 이번에 찾았다면 업데이트
                    if (albumEntity.getCoverUrl() == null && finalCoverUrl != null) {
                        albumEntity.setCoverUrl(finalCoverUrl); // Setter 사용 (Album 엔티티에 @Setter 또는 수동 setter 필요)
                        albumRepository.save(albumEntity); // 변경사항 저장
                        System.out.println("기존 앨범 커버 URL 업데이트: " + albumEntity.getTitle());
                    }
                    // --- 앨범 정보 처리 끝 ---


                    // 3. 곡 정보 처리 (DB 중복 확인)
                    String trackTitle = track.getName(); // 곡 제목
                    // DB에 동일한 제목과 아티스트 ID를 가진 곡이 이미 존재하는지 확인
                    if (!songRepository.existsByTitleAndArtistId(trackTitle, artistEntity.getArtistId())) {
                         // 존재하지 않으면 새로운 Song 엔티티 생성
                         // TODO: 아래 URL을 실제 사용하고 싶은 오디오 파일 URL로 변경하세요!
                         String fixedFilePath = "https://archive.org/download/pkmn-dppt-soundtrack/Disc%201/05%20-%20Twinleaf%20Town%20%28Day%29.mp3"; // 예시 URL

                         // Song 엔티티 생성자 확인 (Integer 타입 duration)
                         Song songEntity = new Song(
                                 trackTitle,
                                 artistEntity.getArtistId(), // 저장된 아티스트 ID
                                 albumEntity.getAlbumId(),  // 저장된 앨범 ID
                                 fixedFilePath,            // 고정된 파일 경로 사용
                                 Integer.valueOf(track.getDurationMs() / 1000), // 길이 (Integer 타입 확인)
                                 null // 장르 정보는 null
                         );
                         // Song 엔티티를 DB에 저장하고 결과를 리스트에 추가
                         savedSongs.add(songRepository.save(songEntity));
                         System.out.println("DB 저장 완료: " + trackTitle + " (filePath: " + fixedFilePath + ")");
                    } else {
                        System.out.println("이미 DB에 존재: " + trackTitle + " - " + artistEntity.getName());
                    }
                } // end for loop
            } else {
                 System.out.println("Spotify 검색 결과 없음: " + query);
            }
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            // Spotify API 호출 중 오류 발생 시 처리
            System.err.println("Spotify 트랙 검색 오류: " + e.getMessage());
            e.printStackTrace(); // 전체 스택 트레이스 출력 (디버깅용)

            // 토큰 만료 오류 감지 및 갱신 시도
             if (e instanceof SpotifyWebApiException && ((SpotifyWebApiException) e).getMessage() != null &&
                (((SpotifyWebApiException) e).getMessage().contains("expired") || ((SpotifyWebApiException) e).getMessage().contains("invalid access token"))) {
                 System.out.println("Access Token 만료 감지. 갱신 시도...");
                 refreshAccessToken(); // 토큰 갱신
             }
        } catch (Exception e) {
            // 예상치 못한 다른 오류 처리
            System.err.println("곡 검색 및 저장 중 예상치 못한 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
        // 최종적으로 DB에 저장된 곡 목록 반환
        return savedSongs;
    } // end searchAndSaveTracks

    // Spotify 날짜 문자열(YYYY, YYYY-MM, YYYY-MM-DD)을 LocalDate로 파싱하는 헬퍼 메서드
    private LocalDate parseSpotifyDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) return null; // 빈 문자열도 null 처리
        try {
            // 날짜 형식 길이에 따라 파싱
            if (dateString.length() == 4) { // YYYY
                return LocalDate.parse(dateString + "-01-01", DateTimeFormatter.ISO_LOCAL_DATE);
            } else if (dateString.length() == 7) { // YYYY-MM
                return LocalDate.parse(dateString + "-01", DateTimeFormatter.ISO_LOCAL_DATE);
            } else { // YYYY-MM-DD 또는 그 이상 (앞 10자리만 사용)
                return LocalDate.parse(dateString.substring(0, 10), DateTimeFormatter.ISO_LOCAL_DATE);
            }
        } catch (DateTimeParseException e) {
            // 파싱 실패 시 경고
            System.err.println("날짜 문자열 파싱 불가: " + dateString + " - " + e.getMessage());
            return null;
        } catch (Exception e) { // 그 외 예외 (예: substring 길이 부족)
            System.err.println("날짜 파싱 중 예외 발생: " + dateString + " - " + e.getMessage());
            return null;
        }
    } // end parseSpotifyDate
} // end class SpotifyService
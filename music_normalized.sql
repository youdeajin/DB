-- ===================================
-- 정규화된 음악 플레이어 데이터베이스 스키마
-- ===================================
-- 작성일: 2025
-- 설명: 데이터베이스 정규화 규칙(1NF, 2NF, 3NF)을 준수한 스키마 설계
-- ===================================

-- ===================================
-- 1. 시퀀스 생성
-- ===================================
CREATE SEQUENCE users_seq START WITH 1 INCREMENT BY 1 NOMAXVALUE;
CREATE SEQUENCE artists_seq START WITH 1 INCREMENT BY 1 NOMAXVALUE;
CREATE SEQUENCE albums_seq START WITH 1 INCREMENT BY 1 NOMAXVALUE;
CREATE SEQUENCE songs_seq START WITH 1 INCREMENT BY 1 NOMAXVALUE;
CREATE SEQUENCE playlists_seq START WITH 1 INCREMENT BY 1 NOMAXVALUE;
CREATE SEQUENCE playlist_songs_seq START WITH 1 INCREMENT BY 1 NOMAXVALUE;
CREATE SEQUENCE play_history_seq START WITH 1 INCREMENT BY 1 NOMAXVALUE;
CREATE SEQUENCE genres_seq START WITH 1 INCREMENT BY 1 NOMAXVALUE;
CREATE SEQUENCE song_genres_seq START WITH 1 INCREMENT BY 1 NOMAXVALUE;
CREATE SEQUENCE album_images_seq START WITH 1 INCREMENT BY 1 NOMAXVALUE;
CREATE SEQUENCE artist_images_seq START WITH 1 INCREMENT BY 1 NOMAXVALUE;
CREATE SEQUENCE user_saved_albums_seq START WITH 1 INCREMENT BY 1 NOMAXVALUE;

-- ===================================
-- 2. 핵심 테이블 생성 (참조 순서 고려)
-- ===================================

-- 1) 사용자 테이블 (변경 없음)
CREATE TABLE users (
    user_id NUMBER PRIMARY KEY,
    email VARCHAR2(255) NOT NULL UNIQUE,
    password VARCHAR2(255) NOT NULL,
    nickname VARCHAR2(50),
    joined_at TIMESTAMP DEFAULT SYSTIMESTAMP
);

-- 2) 아티스트 테이블 (변경 없음)
CREATE TABLE artists (
    artist_id NUMBER PRIMARY KEY,
    name VARCHAR2(100) NOT NULL
);

-- 3) 장르 테이블 (신규 추가 - 3NF 정규화)
-- 기존: SONGS 테이블의 genre 컬럼에 문자열로 저장 ('팝, OST', '댄스/팝, 랩/힙합')
-- 문제: 1NF 위반 (원자성), 3NF 위반 (중복 데이터)
-- 해결: 장르를 별도 테이블로 분리하여 정규화
CREATE TABLE genres (
    genre_id NUMBER PRIMARY KEY,
    name VARCHAR2(50) NOT NULL UNIQUE,
    description VARCHAR2(255)
);

-- 4) 앨범 테이블 (artists 참조)
CREATE TABLE albums (
    album_id NUMBER PRIMARY KEY,
    title VARCHAR2(255) NOT NULL,
    artist_id NUMBER,
    release_date DATE,
    FOREIGN KEY (artist_id) REFERENCES artists(artist_id)
);

-- 5) 곡 테이블 (artists, albums 참조)
-- 변경: genre 컬럼 제거 (SONG_GENRES 테이블로 이동)
CREATE TABLE songs (
    song_id NUMBER PRIMARY KEY,
    title VARCHAR2(255) NOT NULL,
    artist_id NUMBER,
    album_id NUMBER,
    file_path VARCHAR2(512) NOT NULL,
    duration_seconds NUMBER,
    -- genre 컬럼 제거됨 (SONG_GENRES 테이블로 이동)
    FOREIGN KEY (artist_id) REFERENCES artists(artist_id),
    FOREIGN KEY (album_id) REFERENCES albums(album_id)
);

-- 6) 곡-장르 연결 테이블 (신규 추가 - 1NF, 3NF 정규화)
-- 기존: SONGS.genre에 '팝, OST' 같은 복합 값 저장
-- 문제: 1NF 위반 (원자성), 다중 값 저장 불가
-- 해결: 다대다 관계를 위한 중간 테이블 생성
CREATE TABLE song_genres (
    song_genre_id NUMBER PRIMARY KEY,
    song_id NUMBER NOT NULL,
    genre_id NUMBER NOT NULL,
    FOREIGN KEY (song_id) REFERENCES songs(song_id) ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES genres(genre_id) ON DELETE CASCADE,
    UNIQUE (song_id, genre_id) -- 중복 방지
);

-- 7) 앨범 이미지 테이블 (신규 추가 - 1NF 정규화)
-- 기존: ALBUMS.cover_url에 단일 이미지 URL 저장
-- 문제: 여러 이미지 저장 불가, 확장성 부족
-- 해결: 별도 테이블로 분리하여 여러 이미지 지원
CREATE TABLE album_images (
    album_image_id NUMBER PRIMARY KEY,
    album_id NUMBER NOT NULL,
    image_url VARCHAR2(512) NOT NULL,
    image_type VARCHAR2(20) DEFAULT 'COVER', -- COVER, BACK, INSIDE 등
    display_order NUMBER DEFAULT 1,
    FOREIGN KEY (album_id) REFERENCES albums(album_id) ON DELETE CASCADE
);

-- 8) 아티스트 이미지 테이블 (신규 추가 - 1NF 정규화)
-- 기존: ARTISTS 테이블에 이미지 정보 없음
-- 문제: 아티스트 이미지 저장 불가
-- 해결: 별도 테이블로 분리하여 여러 이미지 지원
CREATE TABLE artist_images (
    artist_image_id NUMBER PRIMARY KEY,
    artist_id NUMBER NOT NULL,
    image_url VARCHAR2(512) NOT NULL,
    image_type VARCHAR2(20) DEFAULT 'PROFILE', -- PROFILE, BANNER 등
    display_order NUMBER DEFAULT 1,
    FOREIGN KEY (artist_id) REFERENCES artists(artist_id) ON DELETE CASCADE
);

-- 9) 재생목록 테이블 (users 참조)
CREATE TABLE playlists (
    playlist_id NUMBER PRIMARY KEY,
    title VARCHAR2(255) NOT NULL,
    owner_user_id NUMBER,
    is_public NUMBER(1,0) CHECK (is_public IN (0, 1)),
    created_at TIMESTAMP DEFAULT SYSTIMESTAMP,
    FOREIGN KEY (owner_user_id) REFERENCES users(user_id)
);

-- 10) 재생목록_곡 테이블 (playlists, songs 참조)
CREATE TABLE playlist_songs (
    playlist_song_id NUMBER PRIMARY KEY,
    playlist_id NUMBER,
    song_id NUMBER,
    song_order NUMBER,
    FOREIGN KEY (playlist_id) REFERENCES playlists(playlist_id) ON DELETE CASCADE,
    FOREIGN KEY (song_id) REFERENCES songs(song_id) ON DELETE CASCADE
);

-- 11) 재생 기록 테이블 (users, songs 참조)
CREATE TABLE play_history (
    play_history_id NUMBER PRIMARY KEY,
    user_id NUMBER,
    song_id NUMBER,
    played_at TIMESTAMP DEFAULT SYSTIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (song_id) REFERENCES songs(song_id) ON DELETE CASCADE
);

-- 12) 사용자 저장 앨범 테이블 (users, albums 참조)
CREATE TABLE user_saved_albums (
    saved_id NUMBER PRIMARY KEY,
    user_id NUMBER NOT NULL,
    album_id NUMBER NOT NULL,
    saved_at TIMESTAMP DEFAULT SYSTIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (album_id) REFERENCES albums(album_id) ON DELETE CASCADE,
    UNIQUE (user_id, album_id) -- 중복 저장 방지
);

-- ===================================
-- 3. 인덱스 생성 (성능 최적화)
-- ===================================
CREATE INDEX idx_songs_artist_id ON songs(artist_id);
CREATE INDEX idx_songs_album_id ON songs(album_id);
CREATE INDEX idx_song_genres_song_id ON song_genres(song_id);
CREATE INDEX idx_song_genres_genre_id ON song_genres(genre_id);
CREATE INDEX idx_album_images_album_id ON album_images(album_id);
CREATE INDEX idx_artist_images_artist_id ON artist_images(artist_id);
CREATE INDEX idx_play_history_user_id ON play_history(user_id);
CREATE INDEX idx_play_history_song_id ON play_history(song_id);
CREATE INDEX idx_play_history_played_at ON play_history(played_at DESC);
CREATE INDEX idx_playlist_songs_playlist_id ON playlist_songs(playlist_id);

-- ===================================
COMMIT;


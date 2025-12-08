-- ===================================
-- 기존 데이터베이스에서 정규화된 스키마로 마이그레이션 스크립트
-- ===================================
-- 주의: 이 스크립트는 기존 데이터를 보존하면서 정규화된 스키마로 전환합니다.
-- 실행 전 반드시 백업을 수행하세요.
-- ===================================

-- ===================================
-- 1단계: 새 테이블 생성
-- ===================================

-- 1-1. GENRES 테이블 생성
CREATE TABLE genres (
    genre_id NUMBER PRIMARY KEY,
    name VARCHAR2(50) NOT NULL UNIQUE,
    description VARCHAR2(255)
);

-- 1-2. SONG_GENRES 테이블 생성
CREATE TABLE song_genres (
    song_genre_id NUMBER PRIMARY KEY,
    song_id NUMBER NOT NULL,
    genre_id NUMBER NOT NULL,
    FOREIGN KEY (song_id) REFERENCES songs(song_id) ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES genres(genre_id) ON DELETE CASCADE,
    UNIQUE (song_id, genre_id)
);

-- 1-3. ALBUM_IMAGES 테이블 생성
CREATE TABLE album_images (
    album_image_id NUMBER PRIMARY KEY,
    album_id NUMBER NOT NULL,
    image_url VARCHAR2(512) NOT NULL,
    image_type VARCHAR2(20) DEFAULT 'COVER',
    display_order NUMBER DEFAULT 1,
    FOREIGN KEY (album_id) REFERENCES albums(album_id) ON DELETE CASCADE
);

-- 1-4. ARTIST_IMAGES 테이블 생성
CREATE TABLE artist_images (
    artist_image_id NUMBER PRIMARY KEY,
    artist_id NUMBER NOT NULL,
    image_url VARCHAR2(512) NOT NULL,
    image_type VARCHAR2(20) DEFAULT 'PROFILE',
    display_order NUMBER DEFAULT 1,
    FOREIGN KEY (artist_id) REFERENCES artists(artist_id) ON DELETE CASCADE
);

-- 1-5. 시퀀스 생성
CREATE SEQUENCE genres_seq START WITH 1 INCREMENT BY 1 NOMAXVALUE;
CREATE SEQUENCE song_genres_seq START WITH 1 INCREMENT BY 1 NOMAXVALUE;
CREATE SEQUENCE album_images_seq START WITH 1 INCREMENT BY 1 NOMAXVALUE;
CREATE SEQUENCE artist_images_seq START WITH 1 INCREMENT BY 1 NOMAXVALUE;

-- ===================================
-- 2단계: 기존 데이터 마이그레이션
-- ===================================

-- 2-1. GENRES 테이블에 기존 장르 데이터 추출 및 삽입
-- 쉼표로 구분된 장르 문자열을 분리하여 GENRES 테이블에 삽입
INSERT INTO genres (genre_id, name)
SELECT DISTINCT genres_seq.NEXTVAL, TRIM(genre_name)
FROM (
    SELECT DISTINCT TRIM(REGEXP_SUBSTR(genre, '[^,]+', 1, LEVEL)) AS genre_name
    FROM songs
    WHERE genre IS NOT NULL
    CONNECT BY REGEXP_SUBSTR(genre, '[^,]+', 1, LEVEL) IS NOT NULL
      AND PRIOR song_id = song_id
      AND PRIOR SYS_GUID() IS NOT NULL
)
WHERE genre_name IS NOT NULL;

-- 2-2. SONG_GENRES 테이블에 곡-장르 관계 데이터 삽입
INSERT INTO song_genres (song_genre_id, song_id, genre_id)
SELECT song_genres_seq.NEXTVAL, s.song_id, g.genre_id
FROM songs s
CROSS JOIN (
    SELECT song_id, TRIM(REGEXP_SUBSTR(genre, '[^,]+', 1, LEVEL)) AS genre_name
    FROM songs
    WHERE genre IS NOT NULL
    CONNECT BY REGEXP_SUBSTR(genre, '[^,]+', 1, LEVEL) IS NOT NULL
      AND PRIOR song_id = song_id
      AND PRIOR SYS_GUID() IS NOT NULL
) genre_list ON s.song_id = genre_list.song_id
JOIN genres g ON g.name = genre_list.genre_name
WHERE s.genre IS NOT NULL;

-- 2-3. ALBUM_IMAGES 테이블에 기존 cover_url 데이터 마이그레이션
-- ALBUMS 테이블에 album_cover_url 컬럼이 있다고 가정
-- (실제 컬럼명은 엔티티 클래스의 ALBUM_COVER_URL에 해당)
INSERT INTO album_images (album_image_id, album_id, image_url, image_type, display_order)
SELECT album_images_seq.NEXTVAL, album_id, album_cover_url, 'COVER', 1
FROM albums
WHERE album_cover_url IS NOT NULL;

-- 참고: ALBUMS 테이블에 album_cover_url 컬럼이 없는 경우,
-- 엔티티 클래스의 coverUrl 필드가 실제로는 다른 방식으로 저장되었을 수 있습니다.
-- 이 경우 해당 컬럼명을 확인하여 수정하세요.

-- ===================================
-- 3단계: 인덱스 생성
-- ===================================
CREATE INDEX idx_song_genres_song_id ON song_genres(song_id);
CREATE INDEX idx_song_genres_genre_id ON song_genres(genre_id);
CREATE INDEX idx_album_images_album_id ON album_images(album_id);
CREATE INDEX idx_artist_images_artist_id ON artist_images(artist_id);

-- ===================================
-- 4단계: 기존 컬럼 제거 (선택사항)
-- ===================================
-- 주의: 애플리케이션 코드를 먼저 수정한 후에만 실행하세요!

-- SONGS 테이블에서 genre 컬럼 제거
-- ALTER TABLE songs DROP COLUMN genre;

-- ALBUMS 테이블에서 album_cover_url 컬럼 제거 (필요시)
-- ALTER TABLE albums DROP COLUMN album_cover_url;

-- ===================================
-- 5단계: 데이터 검증
-- ===================================

-- 5-1. 장르 데이터 검증
SELECT 'Total genres' AS check_type, COUNT(*) AS count FROM genres
UNION ALL
SELECT 'Total song-genre relationships', COUNT(*) FROM song_genres
UNION ALL
SELECT 'Songs with genres', COUNT(DISTINCT song_id) FROM song_genres;

-- 5-2. 앨범 이미지 데이터 검증
SELECT 'Total album images' AS check_type, COUNT(*) AS count FROM album_images
UNION ALL
SELECT 'Albums with images', COUNT(DISTINCT album_id) FROM album_images;

-- ===================================
COMMIT;

-- ===================================
-- 마이그레이션 완료 후 확인 사항
-- ===================================
-- 1. 모든 장르가 GENRES 테이블에 정상적으로 저장되었는지 확인
-- 2. 모든 곡-장르 관계가 SONG_GENRES 테이블에 정상적으로 저장되었는지 확인
-- 3. 앨범 이미지가 ALBUM_IMAGES 테이블에 정상적으로 저장되었는지 확인
-- 4. 애플리케이션 코드가 새로운 스키마에 맞게 수정되었는지 확인
-- 5. 기존 컬럼 제거 전 충분한 테스트 수행


# 데이터베이스 정규화 개선 설명서

## 📋 목차
1. [정규화 개요](#정규화-개요)
2. [기존 스키마의 문제점](#기존-스키마의-문제점)
3. [정규화 개선 사항](#정규화-개선-사항)
4. [변경 사항 상세](#변경-사항-상세)
5. [마이그레이션 가이드](#마이그레이션-가이드)

---

## 정규화 개요

### 정규화란?
데이터베이스 정규화는 데이터의 중복을 최소화하고 데이터 무결성을 보장하기 위한 데이터베이스 설계 기법입니다.

### 정규화 단계
- **1NF (제1정규형)**: 모든 컬럼이 원자값(Atomic Value)을 가져야 함
- **2NF (제2정규형)**: 부분 함수 종속 제거
- **3NF (제3정규형)**: 이행 함수 종속 제거

---

## 기존 스키마의 문제점

### 1. SONGS 테이블의 genre 컬럼 문제

#### 문제점
```sql
-- 기존 스키마
CREATE TABLE songs (
    ...
    genre VARCHAR2(50),  -- '팝, OST', '댄스/팝, 랩/힙합' 같은 복합 값
    ...
);
```

**1NF 위반 (원자성 위반)**
- 하나의 컬럼에 여러 값을 저장 ('팝, OST')
- 쉼표로 구분된 문자열은 원자값이 아님
- 장르별 검색 및 집계가 어려움

**3NF 위반 (중복 데이터)**
- 같은 장르 이름이 여러 곡에 반복 저장
- 장르 정보 수정 시 여러 행을 업데이트해야 함
- 데이터 일관성 문제 발생 가능

**실제 데이터 예시:**
```sql
-- 문제가 있는 데이터
INSERT INTO songs (..., genre) VALUES (..., '팝, OST');
INSERT INTO songs (..., genre) VALUES (..., '팝, OST');  -- 중복
INSERT INTO songs (..., genre) VALUES (..., '댄스/팝, 랩/힙합');
```

### 2. ALBUMS 테이블의 cover_url 문제

#### 문제점
```sql
-- 기존 스키마 (엔티티 클래스 기준)
CREATE TABLE albums (
    ...
    album_cover_url VARCHAR2(512),  -- 단일 이미지만 저장 가능
    ...
);
```

**1NF 위반 (확장성 부족)**
- 하나의 앨범에 여러 이미지 저장 불가
- 커버, 뒷면, 내부 이미지 등 다양한 이미지 지원 불가
- 이미지 타입 구분 불가

### 3. ARTISTS 테이블의 이미지 정보 부재

#### 문제점
- 아티스트 이미지 저장 기능 없음
- 프로필 이미지, 배너 이미지 등 저장 불가

---

## 정규화 개선 사항

### 1. GENRES 테이블 추가 (3NF 정규화)

#### 변경 전
```sql
-- SONGS 테이블에 genre 컬럼
genre VARCHAR2(50)  -- '팝, OST'
```

#### 변경 후
```sql
-- GENRES 테이블 생성
CREATE TABLE genres (
    genre_id NUMBER PRIMARY KEY,
    name VARCHAR2(50) NOT NULL UNIQUE,
    description VARCHAR2(255)
);

-- SONGS 테이블에서 genre 컬럼 제거
-- SONG_GENRES 테이블로 대체
```

**개선 효과:**
- ✅ 장르 정보 중복 제거
- ✅ 장르 정보 일관성 보장
- ✅ 장르별 통계 및 검색 용이
- ✅ 장르 정보 수정 시 한 곳만 수정

### 2. SONG_GENRES 테이블 추가 (1NF, 3NF 정규화)

#### 변경 전
```sql
-- 하나의 컬럼에 여러 값 저장
genre VARCHAR2(50)  -- '팝, OST'
```

#### 변경 후
```sql
-- 다대다 관계를 위한 중간 테이블
CREATE TABLE song_genres (
    song_genre_id NUMBER PRIMARY KEY,
    song_id NUMBER NOT NULL,
    genre_id NUMBER NOT NULL,
    FOREIGN KEY (song_id) REFERENCES songs(song_id),
    FOREIGN KEY (genre_id) REFERENCES genres(genre_id),
    UNIQUE (song_id, genre_id)
);
```

**개선 효과:**
- ✅ 1NF 준수: 각 행이 원자값을 가짐
- ✅ 곡당 여러 장르 저장 가능
- ✅ 장르별 곡 검색 용이
- ✅ 데이터 무결성 보장

**데이터 예시:**
```sql
-- 기존 방식 (문제)
song_id=1, genre='팝, OST'

-- 개선된 방식 (정규화)
song_id=1 → song_genres: (song_id=1, genre_id=1), (song_id=1, genre_id=2)
genres: (genre_id=1, name='팝'), (genre_id=2, name='OST')
```

### 3. ALBUM_IMAGES 테이블 추가 (1NF 정규화)

#### 변경 전
```sql
-- 단일 이미지만 저장
album_cover_url VARCHAR2(512)
```

#### 변경 후
```sql
-- 여러 이미지 저장 가능
CREATE TABLE album_images (
    album_image_id NUMBER PRIMARY KEY,
    album_id NUMBER NOT NULL,
    image_url VARCHAR2(512) NOT NULL,
    image_type VARCHAR2(20) DEFAULT 'COVER',  -- COVER, BACK, INSIDE 등
    display_order NUMBER DEFAULT 1,
    FOREIGN KEY (album_id) REFERENCES albums(album_id)
);
```

**개선 효과:**
- ✅ 하나의 앨범에 여러 이미지 저장 가능
- ✅ 이미지 타입 구분 가능 (커버, 뒷면, 내부 등)
- ✅ 이미지 순서 관리 가능
- ✅ 확장성 향상

### 4. ARTIST_IMAGES 테이블 추가 (1NF 정규화)

#### 변경 전
```sql
-- 이미지 정보 없음
CREATE TABLE artists (
    artist_id NUMBER PRIMARY KEY,
    name VARCHAR2(100) NOT NULL
);
```

#### 변경 후
```sql
-- 아티스트 이미지 저장 가능
CREATE TABLE artist_images (
    artist_image_id NUMBER PRIMARY KEY,
    artist_id NUMBER NOT NULL,
    image_url VARCHAR2(512) NOT NULL,
    image_type VARCHAR2(20) DEFAULT 'PROFILE',  -- PROFILE, BANNER 등
    display_order NUMBER DEFAULT 1,
    FOREIGN KEY (artist_id) REFERENCES artists(artist_id)
);
```

**개선 효과:**
- ✅ 아티스트 이미지 저장 기능 추가
- ✅ 여러 이미지 타입 지원
- ✅ 확장성 향상

---

## 변경 사항 상세

### 테이블 변경 요약

| 테이블명 | 변경 유형 | 주요 변경 사항 |
|---------|---------|--------------|
| **GENRES** | 신규 추가 | 장르 정보를 별도 테이블로 분리 |
| **SONG_GENRES** | 신규 추가 | 곡-장르 다대다 관계 테이블 |
| **ALBUM_IMAGES** | 신규 추가 | 앨범 이미지 저장 테이블 |
| **ARTIST_IMAGES** | 신규 추가 | 아티스트 이미지 저장 테이블 |
| **SONGS** | 수정 | genre 컬럼 제거 |
| **ALBUMS** | 수정 | cover_url 컬럼 제거 (엔티티 기준) |
| **USERS** | 변경 없음 | - |
| **ARTISTS** | 변경 없음 | - |
| **PLAYLISTS** | 변경 없음 | - |
| **PLAYLIST_SONGS** | 변경 없음 | - |
| **PLAY_HISTORY** | 변경 없음 | - |
| **USER_SAVED_ALBUMS** | 변경 없음 | - |

### 정규화 수준 비교

| 항목 | 기존 스키마 | 개선된 스키마 |
|-----|-----------|------------|
| **1NF 준수** | ❌ (genre 복합 값) | ✅ (원자값) |
| **2NF 준수** | ✅ | ✅ |
| **3NF 준수** | ❌ (장르 중복) | ✅ (장르 분리) |
| **확장성** | ⚠️ (제한적) | ✅ (우수) |
| **데이터 무결성** | ⚠️ (보통) | ✅ (우수) |

---

## 마이그레이션 가이드

### 1. 데이터 마이그레이션 SQL

```sql
-- 1단계: GENRES 테이블에 기존 장르 데이터 추출 및 삽입
INSERT INTO genres (genre_id, name)
SELECT DISTINCT genres_seq.NEXTVAL, TRIM(REGEXP_SUBSTR(genre, '[^,]+', 1, LEVEL))
FROM songs
WHERE genre IS NOT NULL
CONNECT BY REGEXP_SUBSTR(genre, '[^,]+', 1, LEVEL) IS NOT NULL
  AND PRIOR song_id = song_id
  AND PRIOR SYS_GUID() IS NOT NULL;

-- 2단계: SONG_GENRES 테이블에 데이터 삽입
INSERT INTO song_genres (song_genre_id, song_id, genre_id)
SELECT song_genres_seq.NEXTVAL, s.song_id, g.genre_id
FROM songs s
CROSS JOIN LATERAL (
    SELECT TRIM(REGEXP_SUBSTR(s.genre, '[^,]+', 1, LEVEL)) AS genre_name
    FROM DUAL
    CONNECT BY REGEXP_SUBSTR(s.genre, '[^,]+', 1, LEVEL) IS NOT NULL
) genre_list
JOIN genres g ON g.name = genre_list.genre_name
WHERE s.genre IS NOT NULL;

-- 3단계: ALBUM_IMAGES 테이블에 기존 cover_url 데이터 마이그레이션
INSERT INTO album_images (album_image_id, album_id, image_url, image_type, display_order)
SELECT album_images_seq.NEXTVAL, album_id, album_cover_url, 'COVER', 1
FROM albums
WHERE album_cover_url IS NOT NULL;

-- 4단계: SONGS 테이블에서 genre 컬럼 제거
ALTER TABLE songs DROP COLUMN genre;

-- 5단계: ALBUMS 테이블에서 cover_url 컬럼 제거 (필요시)
-- ALTER TABLE albums DROP COLUMN album_cover_url;
```

### 2. 애플리케이션 코드 변경 사항

#### 엔티티 클래스 변경

**Song.java**
```java
// 변경 전
@Column(name = "GENRE")
private String genre;

// 변경 후
@OneToMany(mappedBy = "song", cascade = CascadeType.ALL)
private List<SongGenre> genres;
```

**Album.java**
```java
// 변경 전
@Column(name = "ALBUM_COVER_URL")
private String coverUrl;

// 변경 후
@OneToMany(mappedBy = "album", cascade = CascadeType.ALL)
private List<AlbumImage> images;
```

**Artist.java**
```java
// 변경 후 (신규)
@OneToMany(mappedBy = "artist", cascade = CascadeType.ALL)
private List<ArtistImage> images;
```

### 3. 쿼리 변경 예시

#### 장르별 곡 조회

**변경 전:**
```sql
SELECT * FROM songs WHERE genre LIKE '%팝%';
```

**변경 후:**
```sql
SELECT DISTINCT s.* 
FROM songs s
JOIN song_genres sg ON s.song_id = sg.song_id
JOIN genres g ON sg.genre_id = g.genre_id
WHERE g.name = '팝';
```

#### 곡의 모든 장르 조회

**변경 전:**
```sql
SELECT genre FROM songs WHERE song_id = 1;
-- 결과: '팝, OST' (문자열 파싱 필요)
```

**변경 후:**
```sql
SELECT g.name 
FROM genres g
JOIN song_genres sg ON g.genre_id = sg.genre_id
WHERE sg.song_id = 1;
-- 결과: '팝', 'OST' (각각 별도 행)
```

---

## 정규화 효과 요약

### ✅ 개선된 점

1. **데이터 중복 제거**
   - 장르 정보가 GENRES 테이블에 한 번만 저장됨
   - 데이터 일관성 향상

2. **확장성 향상**
   - 곡당 여러 장르 저장 가능
   - 앨범/아티스트당 여러 이미지 저장 가능

3. **쿼리 성능 향상**
   - 인덱스를 활용한 장르별 검색 가능
   - JOIN을 통한 효율적인 데이터 조회

4. **유지보수성 향상**
   - 장르 정보 수정 시 한 곳만 수정
   - 코드 가독성 향상

### ⚠️ 주의사항

1. **JOIN 쿼리 증가**
   - 장르 정보 조회 시 JOIN 필요
   - 적절한 인덱스 설계 필수

2. **마이그레이션 필요**
   - 기존 데이터 마이그레이션 필요
   - 애플리케이션 코드 수정 필요

---

## 결론

정규화를 통해 데이터베이스의 **데이터 무결성**, **확장성**, **유지보수성**이 크게 향상되었습니다. 특히 장르 정보의 정규화를 통해 데이터 중복을 제거하고, 이미지 테이블 분리를 통해 확장성을 확보했습니다.


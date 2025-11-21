-- ===================================
-- Script thêm dữ liệu (Data Insertion Script)
-- ===================================

-- 아티스트 추가
INSERT INTO artists (artist_id, name) VALUES (artists_seq.NEXTVAL, 'AUDREY NUNA');
INSERT INTO artists (artist_id, name) VALUES (artists_seq.NEXTVAL, 'Andrew Choi');
INSERT INTO artists (artist_id, name) VALUES (artists_seq.NEXTVAL, 'Danny Chung');
INSERT INTO artists (artist_id, name) VALUES (artists_seq.NEXTVAL, 'EJAE');
INSERT INTO artists (artist_id, name) VALUES (artists_seq.NEXTVAL, 'HUNTR/X');
INSERT INTO artists (artist_id, name) VALUES (artists_seq.NEXTVAL, 'Jinu');
INSERT INTO artists (artist_id, name) VALUES (artists_seq.NEXTVAL, 'KPop Demon Hunters Cast');
INSERT INTO artists (artist_id, name) VALUES (artists_seq.NEXTVAL, 'Kevin Woo');
INSERT INTO artists (artist_id, name) VALUES (artists_seq.NEXTVAL, 'Marcelo Zarvos(마르첼로 자보스)');
INSERT INTO artists (artist_id, name) VALUES (artists_seq.NEXTVAL, 'NMIXX');
INSERT INTO artists (artist_id, name) VALUES (artists_seq.NEXTVAL, 'Neckwav');
INSERT INTO artists (artist_id, name) VALUES (artists_seq.NEXTVAL, 'REI AMI');
INSERT INTO artists (artist_id, name) VALUES (artists_seq.NEXTVAL, 'Rumi');
INSERT INTO artists (artist_id, name) VALUES (artists_seq.NEXTVAL, 'Saja Boys');
INSERT INTO artists (artist_id, name) VALUES (artists_seq.NEXTVAL, 'TWICE (트와이스)');
INSERT INTO artists (artist_id, name) VALUES (artists_seq.NEXTVAL, 'TWS (투어스)');
INSERT INTO artists (artist_id, name) VALUES (artists_seq.NEXTVAL, 'samUIL Lee');
INSERT INTO artists (artist_id, name) VALUES (artists_seq.NEXTVAL, '멜로망스(MeloMance)');
INSERT INTO artists (artist_id, name) VALUES (artists_seq.NEXTVAL, '죠커스(Jokers)');

-- 2. 앨범 및 곡 추가

-- ============================================================
-- Album: KPop Demon Hunters (Soundtrack from the Netflix Film)
-- ============================================================
INSERT INTO albums (album_id, title, artist_id, release_date) 
VALUES (albums_seq.NEXTVAL, 'KPop Demon Hunters (Soundtrack from the Netflix Film)', 
    (SELECT artist_id FROM artists WHERE name = 'HUNTR/X'), 
    TO_DATE('2025.06.20', 'YYYY.MM.DD'));

-- Tracks for KPop Demon Hunters
INSERT INTO songs (song_id, title, artist_id, album_id, file_path, duration_seconds, genre) 
VALUES (songs_seq.NEXTVAL, 'How It’s Done', 
    (SELECT artist_id FROM artists WHERE name = 'HUNTR/X'), 
    (SELECT album_id FROM albums WHERE title = 'KPop Demon Hunters (Soundtrack from the Netflix Film)'), 
    'https://www.dropbox.com/scl/fi/mi0jg1rknx6e9zxfvpj3r/How-It-s-Done-Official-Lyric-Video-_-KPop-Demon-Hunters-_-Sony-Animation.mp3?rlkey=w0ykc2jysj93hobadi2xeycoz&st=8qok7riv&dl=1', 176, '팝, OST');

INSERT INTO songs (song_id, title, artist_id, album_id, file_path, duration_seconds, genre) 
VALUES (songs_seq.NEXTVAL, 'Soda Pop', 
    (SELECT artist_id FROM artists WHERE name = 'Saja Boys'), 
    (SELECT album_id FROM albums WHERE title = 'KPop Demon Hunters (Soundtrack from the Netflix Film)'), 
    'https://www.dropbox.com/scl/fi/wf913z3d53arrb7oznw8b/Soda-Pop-Official-Lyric-Video-_-KPop-Demon-Hunters-_-Sony-Animation.mp3?rlkey=nsvd31ej3tulybvnkwh16e8pp&st=zlj445ae&dl=1', 150, '팝, OST');

INSERT INTO songs (song_id, title, artist_id, album_id, file_path, duration_seconds, genre) 
VALUES (songs_seq.NEXTVAL, 'Golden', 
    (SELECT artist_id FROM artists WHERE name = 'HUNTR/X'), 
    (SELECT album_id FROM albums WHERE title = 'KPop Demon Hunters (Soundtrack from the Netflix Film)'), 
    'https://www.dropbox.com/scl/fi/or10nkpa9bts97tcpg7in/Golden-Official-Lyric-Video-_-KPop-Demon-Hunters-_-Sony-Animation.mp3?rlkey=dsxzr239txcrwwwlmwg7mf702&st=kct83g2n&dl=1', 194, '팝, OST');

INSERT INTO songs (song_id, title, artist_id, album_id, file_path, duration_seconds, genre) 
VALUES (songs_seq.NEXTVAL, 'Strategy', 
    (SELECT artist_id FROM artists WHERE name = 'TWICE (트와이스)'), 
    (SELECT album_id FROM albums WHERE title = 'KPop Demon Hunters (Soundtrack from the Netflix Film)'), 
    'https://www.dropbox.com/scl/fi/at3m2s0no119tm6fpips9/Strategy-KPop-Demon-Hunters-Soundtrack-from-the-Netflix-Film-_-TWICE.mp3?rlkey=o98guqdxz5muz6nd1u2hjb18q&st=y7hxgs6a&dl=1', 168, '팝, OST');

INSERT INTO songs (song_id, title, artist_id, album_id, file_path, duration_seconds, genre) 
VALUES (songs_seq.NEXTVAL, 'Takedown', 
    (SELECT artist_id FROM artists WHERE name = 'HUNTR/X'), 
    (SELECT album_id FROM albums WHERE title = 'KPop Demon Hunters (Soundtrack from the Netflix Film)'), 
    'https://www.dropbox.com/scl/fi/a9f38rpttykzk37ggn2z6/Takedown-Official-Lyric-Video-feat.-Jeongyeon-Jihyo-and-Chaeyoung-from-TWICE-_-KPop-Demon-Hunters.mp3?rlkey=74e90d5td6lg6kvy2uu8vogld&st=bfhexzwd&dl=1', 182, '팝, OST');

INSERT INTO songs (song_id, title, artist_id, album_id, file_path, duration_seconds, genre) 
VALUES (songs_seq.NEXTVAL, 'Your Idol', 
    (SELECT artist_id FROM artists WHERE name = 'Saja Boys'), 
    (SELECT album_id FROM albums WHERE title = 'KPop Demon Hunters (Soundtrack from the Netflix Film)'), 
    'https://www.dropbox.com/scl/fi/020rfyvmxu7m43ow67pk6/Your-Idol-_-Official-Song-Clip-_-KPop-Demon-Hunters-_-Sony-Animation.mp3?rlkey=svr6rs0hdrrv5igj5w8tuwqtt&st=3xzhgw5l&dl=1', 191, '팝, OST');

INSERT INTO songs (song_id, title, artist_id, album_id, file_path, duration_seconds, genre) 
VALUES (songs_seq.NEXTVAL, 'Free', 
    (SELECT artist_id FROM artists WHERE name = 'Rumi'), 
    (SELECT album_id FROM albums WHERE title = 'KPop Demon Hunters (Soundtrack from the Netflix Film)'), 
    'https://www.dropbox.com/scl/fi/relmh23u9dlpz4drkw8hz/Free-_-Official-Lyric-Video-_-Sony-Animation.mp3?rlkey=pg5b73pkdg5y44sx29yo17a3f&st=pwuwgnd0&dl=1', 188, '팝, OST');

INSERT INTO songs (song_id, title, artist_id, album_id, file_path, duration_seconds, genre) 
VALUES (songs_seq.NEXTVAL, 'What It Sounds Like', 
    (SELECT artist_id FROM artists WHERE name = 'HUNTR/X'), 
    (SELECT album_id FROM albums WHERE title = 'KPop Demon Hunters (Soundtrack from the Netflix Film)'), 
    'https://www.dropbox.com/scl/fi/1c9ffouxmxvtn504280e8/What-It-Sounds-Like-_-Official-Song-Clip-_-KPop-Demon-Hunters-_-Sony-Animation.mp3?rlkey=upy0xn9p84h1qaq05hpcr3u1p&st=yxm7dnp7&dl=1', 250, '팝, OST');

INSERT INTO songs (song_id, title, artist_id, album_id, file_path, duration_seconds, genre) 
VALUES (songs_seq.NEXTVAL, '사랑인가 봐 Love, Maybe', 
    (SELECT artist_id FROM artists WHERE name = '멜로망스(MeloMance)'), 
    (SELECT album_id FROM albums WHERE title = 'KPop Demon Hunters (Soundtrack from the Netflix Film)'), 
    'https://www.dropbox.com/scl/fi/2n74avppm742yfr4xc47m/Love-Maybe-KPop-Demon-Hunters-Soundtrack-from-the-Netflix-Film-_-MeloMance.mp3?rlkey=dfnrixgq5g13b1bwbgfjla1l1&st=9khaoft3&dl=1', 185, '팝, OST');

INSERT INTO songs (song_id, title, artist_id, album_id, file_path, duration_seconds, genre) 
VALUES (songs_seq.NEXTVAL, '오솔길 Path', 
    (SELECT artist_id FROM artists WHERE name = '죠커스(Jokers)'), 
    (SELECT album_id FROM albums WHERE title = 'KPop Demon Hunters (Soundtrack from the Netflix Film)'), 
    'https://www.dropbox.com/scl/fi/66gxh26faneb72n7l2p1y/Path-KPop-Demon-Hunters-Soundtrack-from-the-Netflix-Film-_-Jokers.mp3?rlkey=bgtw27d6b56m37500l8djei95&st=1feqvgak&dl=1', 221, '팝, OST');

INSERT INTO songs (song_id, title, artist_id, album_id, file_path, duration_seconds, genre) 
VALUES (songs_seq.NEXTVAL, 'Score Suite', 
    (SELECT artist_id FROM artists WHERE name = 'Marcelo Zarvos(마르첼로 자보스)'), 
    (SELECT album_id FROM albums WHERE title = 'KPop Demon Hunters (Soundtrack from the Netflix Film)'), 
    'https://www.dropbox.com/scl/fi/r9jh65dgt9ly6kvpw1aaq/Score-Suite-KPop-Demon-Hunters-Soundtrack-from-the-Netflix-Film-_-Marcelo-Zarvos.mp3?rlkey=pgnkh64nq16m4idpv4a2alefz&st=jym2tswk&dl=1', 180, '팝, OST');

-- ============================================================
-- Album: TWS 4th Mini Album ‘play hard’
-- ============================================================
INSERT INTO albums (album_id, title, artist_id, release_date) 
VALUES (albums_seq.NEXTVAL, 'TWS 4th Mini Album ‘play hard’', 
    (SELECT artist_id FROM artists WHERE name = 'TWS (투어스)'), 
    TO_DATE('2025.10.13', 'YYYY.MM.DD'));

-- Tracks for TWS 4th Mini Album
INSERT INTO songs (song_id, title, artist_id, album_id, file_path, duration_seconds, genre) 
VALUES (songs_seq.NEXTVAL, 'Head Shoulders Knees Toes', 
    (SELECT artist_id FROM artists WHERE name = 'TWS (투어스)'), 
    (SELECT album_id FROM albums WHERE title = 'TWS 4th Mini Album ‘play hard’'), 
    'https://www.dropbox.com/scl/fi/ubf6yf62uigcx0bxz303p/TWS-Head-Shoulders-Knees-Toes-Official-MV.mp3?rlkey=zqkg7nzhr3zx4p2isf9egppvg&st=m0jwg3vt&dl=1', 178, '댄스/팝, 랩/힙합');

INSERT INTO songs (song_id, title, artist_id, album_id, file_path, duration_seconds, genre) 
VALUES (songs_seq.NEXTVAL, 'OVERDRIVE', 
    (SELECT artist_id FROM artists WHERE name = 'TWS (투어스)'), 
    (SELECT album_id FROM albums WHERE title = 'TWS 4th Mini Album ‘play hard’'), 
    'https://www.dropbox.com/scl/fi/ugk2svrbitz4zbhmv4bi0/TWS-OVERDRIVE-Official-MV.mp3?rlkey=j9bw7sqlaflla4k8232vpck0y&st=flr5ra9d&dl=1', 160, '댄스/팝, 랩/힙합');

-- ============================================================
-- Album: Blue Valentine
-- ============================================================
INSERT INTO albums (album_id, title, artist_id, release_date) 
VALUES (albums_seq.NEXTVAL, 'Blue Valentine', 
    (SELECT artist_id FROM artists WHERE name = 'NMIXX'), 
    TO_DATE('2025.10.13', 'YYYY.MM.DD'));

-- Tracks for Blue Valentine
INSERT INTO songs (song_id, title, artist_id, album_id, file_path, duration_seconds, genre) 
VALUES (songs_seq.NEXTVAL, 'Blue Valentine', 
    (SELECT artist_id FROM artists WHERE name = 'NMIXX'), 
    (SELECT album_id FROM albums WHERE title = 'Blue Valentine'), 
    'https://www.dropbox.com/scl/fi/qcelye7b69x1nxtmxatmd/NMIXX-Blue-Valentine-Core_-SPINNIN-ON-IT.mp3?rlkey=rf5h0i2q3ycr967zdslqytu3h&st=z17bcpxl&dl=1', 186, '댄스/팝');

INSERT INTO songs (song_id, title, artist_id, album_id, file_path, duration_seconds, genre) 
VALUES (songs_seq.NEXTVAL, 'Phoenix', 
    (SELECT artist_id FROM artists WHERE name = 'NMIXX'), 
    (SELECT album_id FROM albums WHERE title = 'Blue Valentine'), 
    'https://www.dropbox.com/scl/fi/dbpp0547jukijl248e4q8/NMIXX-Phoenix-Official-Audio.mp3?rlkey=ivjsoju3ughxlfdxylwfhv3rt&st=42guwscy&dl=1', 164, '댄스/팝');

INSERT INTO songs (song_id, title, artist_id, album_id, file_path, duration_seconds, genre) 
VALUES (songs_seq.NEXTVAL, 'Reality Hurts', 
    (SELECT artist_id FROM artists WHERE name = 'NMIXX'), 
    (SELECT album_id FROM albums WHERE title = 'Blue Valentine'), 
    'https://www.dropbox.com/scl/fi/yvrusasilqyy4c3fywh8v/NMIXX-Reality-Hurts-Track-Video.mp3?rlkey=3v1udgmdx7db8ywtmj7cdzsh8&st=rfvb6xre&dl=1', 155, '댄스/팝');

INSERT INTO songs (song_id, title, artist_id, album_id, file_path, duration_seconds, genre) 
VALUES (songs_seq.NEXTVAL, 'RICO', 
    (SELECT artist_id FROM artists WHERE name = 'NMIXX'), 
    (SELECT album_id FROM albums WHERE title = 'Blue Valentine'), 
    'https://www.dropbox.com/scl/fi/6tsjyhbctxdo15v7df3jc/NMIXX-RICO-Official-Audio.mp3?rlkey=iiqpffh4ai94mp2jjx8bapj0d&st=nf4z36no&dl=1', 152, '댄스/팝');

INSERT INTO songs (song_id, title, artist_id, album_id, file_path, duration_seconds, genre) 
VALUES (songs_seq.NEXTVAL, 'PODIUM', 
    (SELECT artist_id FROM artists WHERE name = 'NMIXX'), 
    (SELECT album_id FROM albums WHERE title = 'Blue Valentine'), 
    'https://www.dropbox.com/scl/fi/1gcdba2p4ifgqgm9fw8v6/NMIXX-PODIUM-Official-Audio.mp3?rlkey=29dqqi5hea96r7ddro1tzg0ue&st=ff361y5i&dl=1', 181, '댄스/팝');

INSERT INTO songs (song_id, title, artist_id, album_id, file_path, duration_seconds, genre) 
VALUES (songs_seq.NEXTVAL, 'Shape of Love', 
    (SELECT artist_id FROM artists WHERE name = 'NMIXX'), 
    (SELECT album_id FROM albums WHERE title = 'Blue Valentine'), 
    'https://www.dropbox.com/scl/fi/qpooq4ey8tgtcuxt7ztif/NMIXX-Shape-of-Love-Official-Audio.mp3?rlkey=xsog3uoydv97ytsd8vwn48qze&st=cfyerbli&dl=1', 172, '댄스/팝');

INSERT INTO songs (song_id, title, artist_id, album_id, file_path, duration_seconds, genre) 
VALUES (songs_seq.NEXTVAL, 'O.O Part 2 (Superhero)', 
    (SELECT artist_id FROM artists WHERE name = 'NMIXX'), 
    (SELECT album_id FROM albums WHERE title = 'Blue Valentine'), 
    'https://www.dropbox.com/scl/fi/g3qvc0nf7gp7k8k592iva/NMIXX-O.O-Part-2-Superhero-Official-Audio.mp3?rlkey=6x0athk0wtfpzg5lvg9uy45m9&st=pat4q0u3&dl=1', 176, '댄스/팝');

COMMIT;
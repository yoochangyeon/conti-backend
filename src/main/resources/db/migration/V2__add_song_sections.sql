-- V2: 곡 구조(Song Sections) 테이블 추가
CREATE TABLE IF NOT EXISTS song_sections (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    song_id BIGINT NOT NULL,
    section_type VARCHAR(20) NOT NULL,
    order_index INT NOT NULL,
    label VARCHAR(100),
    chords TEXT,
    build_up_level INT,
    memo TEXT,
    INDEX idx_song_sections_song_id (song_id),
    INDEX idx_song_sections_order (song_id, order_index)
);

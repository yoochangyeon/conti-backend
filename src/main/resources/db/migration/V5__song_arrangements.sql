-- V5: 다중 편곡 (Song Arrangements) 시스템
-- 곡별 편곡(버전) 관리 + 기존 섹션 연결

-- ============================================================
-- 1. SONG ARRANGEMENTS 테이블
-- ============================================================
CREATE TABLE IF NOT EXISTS song_arrangements (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    song_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    song_key VARCHAR(10),
    bpm INT,
    meter VARCHAR(10),
    duration_minutes INT,
    description TEXT,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_song_arrangements_song_id (song_id),
    INDEX idx_song_arrangements_default (song_id, is_default)
);

-- ============================================================
-- 2. SONG SECTIONS 에 arrangement_id 컬럼 추가
-- ============================================================
ALTER TABLE song_sections ADD COLUMN arrangement_id BIGINT NULL AFTER song_id;
ALTER TABLE song_sections ADD INDEX idx_song_sections_arrangement_id (arrangement_id);

-- ============================================================
-- 3. 데이터 마이그레이션: 기존 곡에 대해 기본 편곡 생성 + 섹션 연결
-- ============================================================
-- 기존 곡마다 기본 편곡 레코드 생성
INSERT INTO song_arrangements (song_id, name, song_key, bpm, is_default, created_at, updated_at)
SELECT s.id, '기본 편곡', s.original_key, s.bpm, TRUE, NOW(), NOW()
FROM songs s;

-- 기존 섹션을 해당 곡의 기본 편곡에 연결
UPDATE song_sections ss
JOIN song_arrangements sa ON sa.song_id = ss.song_id AND sa.is_default = TRUE
SET ss.arrangement_id = sa.id;

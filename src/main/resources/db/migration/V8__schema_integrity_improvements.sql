-- V8: 스키마 무결성 개선
-- 1) setlist_items / setlist_template_items 타임스탬프 추가
-- 2) TIMESTAMP vs DATETIME 통일 (team_notices, setlist_notes → TIMESTAMP)
-- 3) song_tags 유니크 제약조건
-- 4) blockout_dates CHECK 제약조건
-- 5) notifications 복합 인덱스 최적화

-- ============================================================
-- 1. SETLIST ITEMS 타임스탬프 추가
-- ============================================================
ALTER TABLE setlist_items
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- ============================================================
-- 2. SETLIST TEMPLATE ITEMS 타임스탬프 추가
-- ============================================================
ALTER TABLE setlist_template_items
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- ============================================================
-- 3. TIMESTAMP/DATETIME 통일 (V7 테이블을 TIMESTAMP로 변환)
-- ============================================================
ALTER TABLE team_notices
    MODIFY COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    MODIFY COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

ALTER TABLE setlist_notes
    MODIFY COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    MODIFY COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- ============================================================
-- 4. SONG TAGS 유니크 제약조건 (중복 태그 방지)
-- ============================================================
-- 혹시 중복이 있을 경우 대비: 높은 id 삭제 후 유니크 추가
DELETE t1 FROM song_tags t1
INNER JOIN song_tags t2
    ON t1.song_id = t2.song_id AND t1.tag = t2.tag AND t1.id > t2.id;

ALTER TABLE song_tags
    ADD UNIQUE KEY uk_song_tag (song_id, tag);

-- ============================================================
-- 5. BLOCKOUT DATES CHECK 제약조건 (end_date >= start_date)
-- ============================================================
ALTER TABLE blockout_dates
    ADD CONSTRAINT chk_blockout_valid_range CHECK (end_date >= start_date);

-- ============================================================
-- 6. NOTIFICATIONS 복합 인덱스 최적화 (안 읽은 알림 조회)
-- ============================================================
-- 기존 idx_notifications_user_read(user_id, is_read)를 확장
DROP INDEX idx_notifications_user_read ON notifications;
CREATE INDEX idx_notifications_user_read ON notifications (user_id, is_read, created_at DESC);

-- V3: 스케줄링, 포지션, 블록아웃, 예배타입 정규화, 서비스 순서 확장
-- Planning Center 기능 확장

-- ============================================================
-- 1. WORSHIP TYPE 정규화 (한글 → 영문 Enum)
-- ============================================================
UPDATE setlists SET worship_type = 'SUNDAY_1ST' WHERE worship_type IN ('주일 1부 예배', '주일1부', '주일1부예배');
UPDATE setlists SET worship_type = 'SUNDAY_2ND' WHERE worship_type IN ('주일 2부 예배', '주일2부', '주일2부예배');
UPDATE setlists SET worship_type = 'SUNDAY_3RD' WHERE worship_type IN ('주일 3부 예배', '주일3부', '주일3부예배');
UPDATE setlists SET worship_type = 'WEDNESDAY' WHERE worship_type IN ('수요예배', '수요 예배');
UPDATE setlists SET worship_type = 'FRIDAY' WHERE worship_type IN ('금요예배', '금요 예배', '금요기도회', '금요 기도회');
UPDATE setlists SET worship_type = 'DAWN' WHERE worship_type IN ('새벽예배', '새벽 예배', '새벽기도');
UPDATE setlists SET worship_type = 'YOUTH' WHERE worship_type IN ('청년예배', '청년 예배');
UPDATE setlists SET worship_type = 'RETREAT' WHERE worship_type IN ('수련회', '수련회 예배');
UPDATE setlists SET worship_type = 'SPECIAL' WHERE worship_type IN ('특별예배', '특별 예배', '특별집회');
UPDATE setlists SET worship_type = 'OTHER'
WHERE worship_type IS NOT NULL
  AND worship_type NOT IN ('SUNDAY_1ST','SUNDAY_2ND','SUNDAY_3RD','WEDNESDAY','FRIDAY','DAWN','YOUTH','RETREAT','SPECIAL','OTHER');

-- ============================================================
-- 2. MEMBER POSITIONS (멤버별 악기/역할)
-- ============================================================
CREATE TABLE IF NOT EXISTS member_positions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    team_member_id BIGINT NOT NULL,
    position VARCHAR(30) NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE KEY uk_member_position (team_member_id, position),
    INDEX idx_member_positions_team_member_id (team_member_id),
    INDEX idx_member_positions_position (position)
);

-- ============================================================
-- 3. BLOCKOUT DATES (부재 일정)
-- ============================================================
CREATE TABLE IF NOT EXISTS blockout_dates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    team_member_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_blockout_dates_team_member_id (team_member_id),
    INDEX idx_blockout_dates_range (team_member_id, start_date, end_date),
    INDEX idx_blockout_dates_start (start_date),
    INDEX idx_blockout_dates_end (end_date)
);

-- ============================================================
-- 4. SERVICE SCHEDULES (봉사 스케줄)
-- ============================================================
CREATE TABLE IF NOT EXISTS service_schedules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    setlist_id BIGINT NOT NULL,
    team_member_id BIGINT NOT NULL,
    position VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    declined_reason VARCHAR(200),
    notified_at TIMESTAMP NULL,
    responded_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_service_schedule (setlist_id, team_member_id, position),
    INDEX idx_service_schedules_setlist_id (setlist_id),
    INDEX idx_service_schedules_team_member_id (team_member_id),
    INDEX idx_service_schedules_status (status),
    INDEX idx_service_schedules_setlist_status (setlist_id, status)
);

-- ============================================================
-- 5. SETLIST ITEMS 확장 (비찬양 항목 지원)
-- ============================================================
ALTER TABLE setlist_items MODIFY COLUMN song_id BIGINT NULL;
ALTER TABLE setlist_items ADD COLUMN item_type VARCHAR(30) NOT NULL DEFAULT 'SONG' AFTER setlist_id;
ALTER TABLE setlist_items ADD COLUMN title VARCHAR(200) NULL AFTER item_type;
ALTER TABLE setlist_items ADD COLUMN duration_minutes INT NULL AFTER song_key;
ALTER TABLE setlist_items ADD INDEX idx_setlist_items_type (setlist_id, item_type);

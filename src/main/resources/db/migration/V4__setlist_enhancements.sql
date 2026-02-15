-- V4: 콘티(세트리스트) 고도화 - 색상 코딩, 서비스 구간, 플랜 템플릿

-- ============================================================
-- 1. SETLIST ITEMS 확장 (색상 + 서비스 구간)
-- ============================================================
ALTER TABLE setlist_items ADD COLUMN color VARCHAR(20) NULL;
ALTER TABLE setlist_items ADD COLUMN service_phase VARCHAR(20) NULL;

-- ============================================================
-- 2. SETLIST TEMPLATES (플랜 템플릿)
-- ============================================================
CREATE TABLE IF NOT EXISTS setlist_templates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    team_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    worship_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_setlist_templates_team_id (team_id)
);

-- ============================================================
-- 3. SETLIST TEMPLATE ITEMS (템플릿 항목)
-- ============================================================
CREATE TABLE IF NOT EXISTS setlist_template_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    template_id BIGINT NOT NULL,
    item_type VARCHAR(30) NOT NULL DEFAULT 'SONG',
    order_index INT NOT NULL,
    song_id BIGINT NULL,
    title VARCHAR(200),
    description VARCHAR(500),
    duration_minutes INT NULL,
    color VARCHAR(20) NULL,
    service_phase VARCHAR(20) NULL,

    INDEX idx_setlist_template_items_template_id (template_id)
);

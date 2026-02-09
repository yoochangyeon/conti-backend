-- Conti 초기 스키마
-- V1: 9개 핵심 테이블 생성

-- 1. users (사용자)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    profile_image VARCHAR(500),
    provider VARCHAR(20) NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_provider (provider, provider_id)
);

-- 2. teams (팀)
CREATE TABLE IF NOT EXISTS teams (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    invite_code VARCHAR(20) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 3. team_members (팀 멤버)
CREATE TABLE IF NOT EXISTS team_members (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    team_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'VIEWER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE KEY uk_user_team (user_id, team_id),
    INDEX idx_user_id (user_id),
    INDEX idx_team_id (team_id)
);

-- 4. songs (찬양)
CREATE TABLE IF NOT EXISTS songs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    team_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    artist VARCHAR(100),
    original_key VARCHAR(10),
    bpm INT,
    memo TEXT,
    youtube_url VARCHAR(500),
    music_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_team_id (team_id),
    INDEX idx_team_title (team_id, title)
);

-- 5. song_tags (찬양 태그)
CREATE TABLE IF NOT EXISTS song_tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    song_id BIGINT NOT NULL,
    tag VARCHAR(50) NOT NULL,

    INDEX idx_song_id (song_id),
    INDEX idx_tag (tag)
);

-- 6. song_files (악보 파일)
CREATE TABLE IF NOT EXISTS song_files (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    song_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_type VARCHAR(20),
    file_size BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_song_id (song_id)
);

-- 7. song_usages (곡 사용 이력)
CREATE TABLE IF NOT EXISTS song_usages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    song_id BIGINT NOT NULL,
    setlist_id BIGINT NOT NULL,
    used_key VARCHAR(10),
    leader_id BIGINT,
    used_at DATE NOT NULL,

    INDEX idx_song_id (song_id),
    INDEX idx_setlist_id (setlist_id),
    INDEX idx_song_usage (song_id, used_at),
    INDEX idx_leader_usage (leader_id, used_at)
);

-- 8. setlists (콘티)
CREATE TABLE IF NOT EXISTS setlists (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    team_id BIGINT NOT NULL,
    creator_id BIGINT NOT NULL,
    title VARCHAR(200),
    worship_date DATE NOT NULL,
    worship_type VARCHAR(50),
    leader_id BIGINT,
    memo TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_team_id (team_id),
    INDEX idx_team_date (team_id, worship_date),
    INDEX idx_creator_id (creator_id),
    INDEX idx_leader_id (leader_id)
);

-- 9. setlist_items (콘티 아이템)
CREATE TABLE IF NOT EXISTS setlist_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    setlist_id BIGINT NOT NULL,
    song_id BIGINT NOT NULL,
    order_index INT NOT NULL,
    song_key VARCHAR(10),
    memo TEXT,

    INDEX idx_setlist_id (setlist_id),
    INDEX idx_song_id (song_id),
    INDEX idx_setlist_order (setlist_id, order_index)
);

-- Team Notices
CREATE TABLE team_notices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    is_pinned BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_team_notices_team_id ON team_notices (team_id);

-- Setlist Notes (position-specific)
CREATE TABLE setlist_notes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    setlist_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    position VARCHAR(50) NULL,
    content TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_setlist_notes_setlist_id ON setlist_notes (setlist_id);

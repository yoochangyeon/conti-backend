package com.conti.domain.team.entity;

public enum TeamRole {
    ADMIN(4),
    EDITOR(3),
    SCHEDULER(2),
    VIEWER(1),
    GUEST(0);

    private final int level;

    TeamRole(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public boolean isAtLeast(TeamRole required) {
        return this.level >= required.level;
    }
}

package com.conti.domain.setlist.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ServicePhase {
    BEFORE("예배 전"),
    DURING("예배 중"),
    AFTER("예배 후");

    private final String displayName;
}

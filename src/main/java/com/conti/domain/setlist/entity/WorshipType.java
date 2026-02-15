package com.conti.domain.setlist.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WorshipType {
    SUNDAY_1ST("주일 1부 예배"),
    SUNDAY_2ND("주일 2부 예배"),
    SUNDAY_3RD("주일 3부 예배"),
    WEDNESDAY("수요 예배"),
    FRIDAY("금요 예배"),
    DAWN("새벽 예배"),
    YOUTH("청년 예배"),
    RETREAT("수련회"),
    SPECIAL("특별 예배"),
    OTHER("기타");

    private final String displayName;
}

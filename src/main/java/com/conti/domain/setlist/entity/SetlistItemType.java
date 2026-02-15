package com.conti.domain.setlist.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SetlistItemType {
    SONG("찬양"),
    PRAYER("기도"),
    SERMON("설교"),
    OFFERING("헌금"),
    ANNOUNCEMENT("광고"),
    SCRIPTURE("성경 봉독"),
    CREED("사도신경"),
    BENEDICTION("축도"),
    PRELUDE("전주"),
    POSTLUDE("후주"),
    TRANSITION("전환"),
    HEADER("헤더"),
    CUSTOM("기타");

    private final String displayName;
}

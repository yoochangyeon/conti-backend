package com.conti.domain.team.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Position {
    WORSHIP_LEADER("인도자"),
    VOCAL("보컬"),
    ACOUSTIC_GUITAR("어쿠스틱 기타"),
    ELECTRIC_GUITAR("일렉 기타"),
    BASS("베이스"),
    DRUM("드럼"),
    KEYBOARD("키보드"),
    PIANO("피아노"),
    SYNTH("신디사이저"),
    PAD("패드"),
    VIOLIN("바이올린"),
    CELLO("첼로"),
    FLUTE("플룻"),
    SOUND("음향"),
    VISUAL("영상"),
    OTHER("기타");

    private final String displayName;
}

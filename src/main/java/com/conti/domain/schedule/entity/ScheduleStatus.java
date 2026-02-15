package com.conti.domain.schedule.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ScheduleStatus {
    PENDING("대기"),
    ACCEPTED("수락"),
    DECLINED("거절");

    private final String displayName;
}

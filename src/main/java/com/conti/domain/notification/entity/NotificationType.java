package com.conti.domain.notification.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationType {

    SCHEDULE_ASSIGNED("봉사 배정"),
    SCHEDULE_RESPONSE("배정 응답"),
    SCHEDULE_REMINDER("봉사 리마인더"),
    SETLIST_UPDATED("콘티 수정");

    private final String displayName;
}

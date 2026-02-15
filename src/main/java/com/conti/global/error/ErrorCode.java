package com.conti.global.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT(400, "C001", "잘못된 입력값입니다"),
    INTERNAL_ERROR(500, "C002", "서버 내부 오류가 발생했습니다"),

    // Auth
    UNAUTHORIZED(401, "A001", "인증이 필요합니다"),
    FORBIDDEN(403, "A002", "권한이 없습니다"),
    INVALID_TOKEN(401, "A003", "유효하지 않은 토큰입니다"),
    EXPIRED_TOKEN(401, "A004", "만료된 토큰입니다"),

    // User
    USER_NOT_FOUND(404, "U001", "사용자를 찾을 수 없습니다"),

    // Team
    TEAM_NOT_FOUND(404, "T001", "팀을 찾을 수 없습니다"),
    DUPLICATE_TEAM_MEMBER(409, "T002", "이미 팀에 속한 멤버입니다"),
    INVALID_INVITE_CODE(400, "T003", "유효하지 않은 초대 코드입니다"),

    // Song
    SONG_NOT_FOUND(404, "S001", "찬양을 찾을 수 없습니다"),

    // Setlist
    SETLIST_NOT_FOUND(404, "SL001", "콘티를 찾을 수 없습니다"),

    // File
    FILE_UPLOAD_FAILED(500, "F001", "파일 업로드에 실패했습니다"),

    // Schedule
    DUPLICATE_SCHEDULE(409, "SC001", "이미 같은 포지션으로 배정된 멤버입니다"),
    SCHEDULE_NOT_FOUND(404, "SC002", "배정 정보를 찾을 수 없습니다"),
    SCHEDULE_ALREADY_RESPONDED(400, "SC003", "이미 응답한 배정입니다"),
    DECLINE_REASON_REQUIRED(400, "SC004", "거절 사유를 입력해주세요"),
    POSITION_NOT_QUALIFIED(403, "SC005", "해당 포지션 자격이 없습니다"),

    // Blockout
    BLOCKOUT_NOT_FOUND(404, "BD001", "부재 일정을 찾을 수 없습니다"),
    INVALID_DATE_RANGE(400, "BD002", "종료일은 시작일 이후여야 합니다"),

    // SetlistItem
    SONG_ID_REQUIRED(400, "SI001", "찬양 항목에는 곡 ID가 필요합니다"),
    ITEM_TITLE_REQUIRED(400, "SI002", "비찬양 항목에는 제목이 필요합니다"),

    // SetlistTemplate
    TEMPLATE_NOT_FOUND(404, "ST001", "템플릿을 찾을 수 없습니다"),

    // TeamNotice
    NOTICE_NOT_FOUND(404, "TN001", "공지사항을 찾을 수 없습니다"),

    // SetlistNote
    SETLIST_NOTE_NOT_FOUND(404, "SN001", "노트를 찾을 수 없습니다"),

    // Notification
    NOTIFICATION_NOT_FOUND(404, "N001", "알림을 찾을 수 없습니다");

    private final int httpStatus;
    private final String code;
    private final String message;
}

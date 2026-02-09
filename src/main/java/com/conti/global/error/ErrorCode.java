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
    FILE_UPLOAD_FAILED(500, "F001", "파일 업로드에 실패했습니다");

    private final int httpStatus;
    private final String code;
    private final String message;
}

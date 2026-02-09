package com.conti.domain.setlist.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "콘티 수정 요청")
public record SetlistUpdateRequest(
        @Schema(description = "콘티 제목", example = "2024-01-07 주일예배")
        String title,
        @Schema(description = "예배 날짜", example = "2024-01-07")
        LocalDate worshipDate,
        @Schema(description = "예배 타입", example = "주일예배")
        String worshipType,
        @Schema(description = "인도자 ID", example = "1")
        Long leaderId,
        @Schema(description = "메모")
        String memo
) {
}

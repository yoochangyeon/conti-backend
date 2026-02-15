package com.conti.domain.setlist.dto;

import com.conti.domain.setlist.entity.WorshipType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "콘티 복사 요청")
public record SetlistCopyRequest(
        @Schema(description = "새 콘티 제목", example = "2024-02-04 주일예배")
        String title,
        @Schema(description = "새 예배 날짜", example = "2024-02-04")
        @NotNull LocalDate worshipDate,
        @Schema(description = "예배 타입", example = "SUNDAY_1ST")
        WorshipType worshipType
) {
}

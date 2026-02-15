package com.conti.domain.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "부재 일정 생성 요청")
public record BlockoutDateCreateRequest(
        @Schema(description = "시작일", example = "2026-03-01")
        @NotNull LocalDate startDate,
        @Schema(description = "종료일", example = "2026-03-07")
        @NotNull LocalDate endDate,
        @Schema(description = "사유", example = "해외 출장")
        String reason
) {
}

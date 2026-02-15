package com.conti.domain.schedule.dto;

import com.conti.domain.team.entity.Position;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "셀프 사인업 요청")
public record ScheduleSignupRequest(
        @Schema(description = "포지션", example = "VOCAL")
        @NotNull Position position
) {
}

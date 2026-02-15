package com.conti.domain.schedule.dto;

import com.conti.domain.team.entity.Position;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "스케줄 생성 요청")
public record ScheduleCreateRequest(
        @Schema(description = "팀 멤버 ID", example = "1")
        @NotNull Long teamMemberId,
        @Schema(description = "포지션", example = "VOCAL")
        @NotNull Position position
) {
}

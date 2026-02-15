package com.conti.domain.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "스케줄 일괄 생성 요청")
public record ScheduleBulkCreateRequest(
        @Schema(description = "스케줄 목록")
        @NotEmpty @Valid List<ScheduleCreateRequest> schedules
) {
}

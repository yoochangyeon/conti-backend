package com.conti.domain.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "스케줄 일괄 생성 결과")
public record ScheduleBulkResult(
        @Schema(description = "생성된 스케줄 목록")
        List<ServiceScheduleResponse> created,
        @Schema(description = "충돌 목록")
        List<ScheduleConflictResponse> conflicts
) {
}

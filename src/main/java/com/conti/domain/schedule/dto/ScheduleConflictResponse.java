package com.conti.domain.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "스케줄 충돌 정보")
public record ScheduleConflictResponse(
        @Schema(description = "팀 멤버 ID", example = "1")
        Long teamMemberId,
        @Schema(description = "멤버 이름", example = "홍길동")
        String memberName,
        @Schema(description = "포지션", example = "VOCAL")
        String position,
        @Schema(description = "충돌 유형", example = "BLOCKOUT")
        String conflictType,
        @Schema(description = "충돌 상세", example = "2026-03-01 ~ 2026-03-07 해외 출장")
        String conflictDetail
) {
}

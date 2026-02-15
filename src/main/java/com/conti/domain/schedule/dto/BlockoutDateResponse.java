package com.conti.domain.schedule.dto;

import com.conti.domain.schedule.entity.BlockoutDate;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "부재 일정 응답")
public record BlockoutDateResponse(
        @Schema(description = "부재 일정 ID", example = "1")
        Long id,
        @Schema(description = "팀 멤버 ID", example = "1")
        Long teamMemberId,
        @Schema(description = "시작일", example = "2026-03-01")
        LocalDate startDate,
        @Schema(description = "종료일", example = "2026-03-07")
        LocalDate endDate,
        @Schema(description = "사유", example = "해외 출장")
        String reason
) {

    public static BlockoutDateResponse from(BlockoutDate blockoutDate) {
        return new BlockoutDateResponse(
                blockoutDate.getId(),
                blockoutDate.getTeamMember().getId(),
                blockoutDate.getStartDate(),
                blockoutDate.getEndDate(),
                blockoutDate.getReason()
        );
    }
}

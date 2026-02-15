package com.conti.domain.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "스케줄 응답(수락/거절) 요청")
public record ScheduleRespondRequest(
        @Schema(description = "수락 여부", example = "true")
        boolean accept,
        @Schema(description = "거절 사유", example = "개인 사정으로 참석이 어렵습니다")
        String declinedReason
) {
}

package com.conti.domain.schedule.dto;

import com.conti.domain.schedule.entity.ServiceSchedule;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "봉사 스케줄 응답")
public record ServiceScheduleResponse(
        @Schema(description = "스케줄 ID", example = "1")
        Long id,
        @Schema(description = "콘티 ID", example = "1")
        Long setlistId,
        @Schema(description = "팀 멤버 ID", example = "1")
        Long teamMemberId,
        @Schema(description = "멤버 이름", example = "홍길동")
        String memberName,
        @Schema(description = "프로필 이미지 URL")
        String profileImage,
        @Schema(description = "포지션", example = "VOCAL")
        String position,
        @Schema(description = "포지션 표시명", example = "보컬")
        String positionDisplayName,
        @Schema(description = "상태", example = "PENDING")
        String status,
        @Schema(description = "상태 표시명", example = "대기")
        String statusDisplayName,
        @Schema(description = "거절 사유")
        String declinedReason,
        @Schema(description = "알림 발송 시각")
        LocalDateTime notifiedAt,
        @Schema(description = "응답 시각")
        LocalDateTime respondedAt,
        @Schema(description = "생성 일시")
        LocalDateTime createdAt
) {

    public static ServiceScheduleResponse from(ServiceSchedule schedule) {
        return new ServiceScheduleResponse(
                schedule.getId(),
                schedule.getSetlist().getId(),
                schedule.getTeamMember().getId(),
                schedule.getTeamMember().getUser().getName(),
                schedule.getTeamMember().getUser().getProfileImage(),
                schedule.getPosition().name(),
                schedule.getPosition().getDisplayName(),
                schedule.getStatus().name(),
                schedule.getStatus().getDisplayName(),
                schedule.getDeclinedReason(),
                schedule.getNotifiedAt(),
                schedule.getRespondedAt(),
                schedule.getCreatedAt()
        );
    }
}

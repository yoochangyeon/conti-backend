package com.conti.domain.notification.dto;

import com.conti.domain.notification.entity.Notification;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "알림 응답")
public record NotificationResponse(
        @Schema(description = "알림 ID", example = "1")
        Long id,
        @Schema(description = "알림 유형", example = "SCHEDULE_ASSIGNED")
        String type,
        @Schema(description = "알림 유형 표시명", example = "봉사 배정")
        String typeDisplayName,
        @Schema(description = "제목", example = "봉사 배정 알림")
        String title,
        @Schema(description = "메시지", example = "2025-03-02 주일 예배에 보컬로 배정되었습니다")
        String message,
        @Schema(description = "참조 유형", example = "SETLIST")
        String referenceType,
        @Schema(description = "참조 ID", example = "5")
        Long referenceId,
        @Schema(description = "읽음 여부")
        boolean isRead,
        @Schema(description = "생성 일시")
        LocalDateTime createdAt
) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType().name(),
                notification.getType().getDisplayName(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getReferenceType(),
                notification.getReferenceId(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}

package com.conti.domain.setlist.dto;

import com.conti.domain.setlist.entity.SetlistTemplate;
import com.conti.domain.setlist.entity.SetlistTemplateItem;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "콘티 템플릿 응답")
public record SetlistTemplateResponse(
        @Schema(description = "템플릿 ID", example = "1")
        Long id,
        @Schema(description = "템플릿 이름", example = "주일 1부 기본 콘티")
        String name,
        @Schema(description = "설명")
        String description,
        @Schema(description = "예배 타입", example = "SUNDAY_1ST")
        String worshipType,
        @Schema(description = "예배 타입 표시명", example = "주일 1부 예배")
        String worshipTypeDisplayName,
        @Schema(description = "항목 수")
        int itemCount,
        @Schema(description = "생성 일시")
        LocalDateTime createdAt,
        @Schema(description = "템플릿 항목 목록")
        List<SetlistTemplateItemResponse> items
) {

    public static SetlistTemplateResponse from(SetlistTemplate template) {
        List<SetlistTemplateItemResponse> itemResponses = template.getItems().stream()
                .map(SetlistTemplateItemResponse::from)
                .toList();

        return new SetlistTemplateResponse(
                template.getId(),
                template.getName(),
                template.getDescription(),
                template.getWorshipType() != null ? template.getWorshipType().name() : null,
                template.getWorshipType() != null ? template.getWorshipType().getDisplayName() : null,
                template.getItems().size(),
                template.getCreatedAt(),
                itemResponses
        );
    }

    @Schema(description = "콘티 템플릿 항목 응답")
    public record SetlistTemplateItemResponse(
            @Schema(description = "항목 ID")
            Long id,
            @Schema(description = "항목 타입")
            String itemType,
            @Schema(description = "항목 타입 표시명")
            String itemTypeDisplayName,
            @Schema(description = "순서")
            int orderIndex,
            @Schema(description = "곡 ID")
            Long songId,
            @Schema(description = "제목")
            String title,
            @Schema(description = "설명")
            String description,
            @Schema(description = "진행 시간(분)")
            Integer durationMinutes,
            @Schema(description = "색상 코드")
            String color,
            @Schema(description = "서비스 구간")
            String servicePhase,
            @Schema(description = "서비스 구간 표시명")
            String servicePhaseDisplayName
    ) {
        public static SetlistTemplateItemResponse from(SetlistTemplateItem item) {
            return new SetlistTemplateItemResponse(
                    item.getId(),
                    item.getItemType().name(),
                    item.getItemType().getDisplayName(),
                    item.getOrderIndex(),
                    item.getSong() != null ? item.getSong().getId() : null,
                    item.getTitle(),
                    item.getDescription(),
                    item.getDurationMinutes(),
                    item.getColor(),
                    item.getServicePhase() != null ? item.getServicePhase().name() : null,
                    item.getServicePhase() != null ? item.getServicePhase().getDisplayName() : null
            );
        }
    }
}

package com.conti.domain.setlist.dto;

import com.conti.domain.setlist.entity.ServicePhase;
import com.conti.domain.setlist.entity.SetlistItemType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "콘티 템플릿 항목 요청")
public record SetlistTemplateItemRequest(
        @Schema(description = "항목 타입", example = "SONG")
        SetlistItemType itemType,
        @Schema(description = "곡 ID", example = "1")
        Long songId,
        @Schema(description = "제목", example = "대표 기도")
        String title,
        @Schema(description = "설명")
        String description,
        @Schema(description = "진행 시간(분)", example = "5")
        Integer durationMinutes,
        @Schema(description = "색상 코드", example = "#FF5733")
        String color,
        @Schema(description = "서비스 구간", example = "DURING")
        ServicePhase servicePhase
) {
}

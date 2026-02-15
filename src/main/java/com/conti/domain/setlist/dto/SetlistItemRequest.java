package com.conti.domain.setlist.dto;

import com.conti.domain.setlist.entity.ServicePhase;
import com.conti.domain.setlist.entity.SetlistItemType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "콘티 항목 추가/수정 요청")
public record SetlistItemRequest(
        @Schema(description = "항목 타입 (기본: SONG)", example = "SONG")
        SetlistItemType itemType,
        @Schema(description = "곡 ID (SONG 타입일 때 필수)", example = "1")
        Long songId,
        @Schema(description = "제목 (비찬양 항목일 때 필수)", example = "대표 기도")
        String title,
        @Schema(description = "연주 키", example = "A")
        String songKey,
        @Schema(description = "진행 시간(분)", example = "5")
        Integer durationMinutes,
        @Schema(description = "메모")
        String memo,
        @Schema(description = "색상 코드", example = "#FF5733")
        String color,
        @Schema(description = "서비스 구간", example = "DURING")
        ServicePhase servicePhase
) {
}

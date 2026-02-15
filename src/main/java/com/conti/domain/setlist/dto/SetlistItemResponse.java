package com.conti.domain.setlist.dto;

import com.conti.domain.setlist.entity.SetlistItem;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "콘티 항목 응답")
public record SetlistItemResponse(
        @Schema(description = "아이템 ID", example = "1")
        Long id,
        @Schema(description = "항목 타입", example = "SONG")
        String itemType,
        @Schema(description = "항목 타입 표시명", example = "찬양")
        String itemTypeDisplayName,
        @Schema(description = "곡 ID (찬양 항목만)", example = "1")
        Long songId,
        @Schema(description = "제목", example = "이 땅의 모든 찬양")
        String songTitle,
        @Schema(description = "아티스트 (찬양 항목만)", example = "마커스워십")
        String artist,
        @Schema(description = "순서", example = "1")
        int orderIndex,
        @Schema(description = "연주 키", example = "A")
        String songKey,
        @Schema(description = "진행 시간(분)")
        Integer durationMinutes,
        @Schema(description = "메모")
        String memo,
        @Schema(description = "색상 코드")
        String color,
        @Schema(description = "서비스 구간", example = "DURING")
        String servicePhase,
        @Schema(description = "서비스 구간 표시명", example = "예배 중")
        String servicePhaseDisplayName
) {

    public static SetlistItemResponse from(SetlistItem item) {
        return new SetlistItemResponse(
                item.getId(),
                item.getItemType().name(),
                item.getItemType().getDisplayName(),
                item.getSong() != null ? item.getSong().getId() : null,
                item.getDisplayTitle(),
                item.getSong() != null ? item.getSong().getArtist() : null,
                item.getOrderIndex(),
                item.getSongKey(),
                item.getDurationMinutes(),
                item.getMemo(),
                item.getColor(),
                item.getServicePhase() != null ? item.getServicePhase().name() : null,
                item.getServicePhase() != null ? item.getServicePhase().getDisplayName() : null
        );
    }
}

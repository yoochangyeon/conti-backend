package com.conti.domain.setlist.dto;

import com.conti.domain.setlist.entity.SetlistItem;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "콘티 곡 응답")
public record SetlistItemResponse(
        @Schema(description = "아이템 ID", example = "1")
        Long id,
        @Schema(description = "곡 ID", example = "1")
        Long songId,
        @Schema(description = "곡 제목", example = "이 땅의 모든 찬양")
        String songTitle,
        @Schema(description = "아티스트", example = "마커스워십")
        String artist,
        @Schema(description = "순서", example = "1")
        int orderIndex,
        @Schema(description = "연주 키", example = "A")
        String songKey,
        @Schema(description = "메모")
        String memo
) {

    public static SetlistItemResponse from(SetlistItem item) {
        return new SetlistItemResponse(
                item.getId(),
                item.getSong().getId(),
                item.getSong().getTitle(),
                item.getSong().getArtist(),
                item.getOrderIndex(),
                item.getSongKey(),
                item.getMemo()
        );
    }
}

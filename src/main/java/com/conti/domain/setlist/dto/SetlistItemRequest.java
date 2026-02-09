package com.conti.domain.setlist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "콘티 곡 추가/수정 요청")
public record SetlistItemRequest(
        @Schema(description = "곡 ID", example = "1")
        @NotNull Long songId,
        @Schema(description = "연주 키", example = "A")
        String songKey,
        @Schema(description = "메모")
        String memo
) {
}

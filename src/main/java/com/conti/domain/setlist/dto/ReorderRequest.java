package com.conti.domain.setlist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "콘티 곡 순서 변경 요청")
public record ReorderRequest(
        @Schema(description = "정렬된 아이템 ID 목록", example = "[3, 1, 2]")
        @NotEmpty List<Long> itemIds
) {
}

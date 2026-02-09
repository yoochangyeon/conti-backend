package com.conti.domain.song.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "태그 응답")
public record TagResponse(
        @Schema(description = "태그 이름", example = "찬양")
        String tag,
        @Schema(description = "사용 횟수", example = "15")
        long count
) {
}

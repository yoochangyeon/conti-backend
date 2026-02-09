package com.conti.domain.song.dto;

import com.conti.domain.song.entity.SongUsage;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "찬양 사용 이력 응답")
public record SongUsageResponse(
        @Schema(description = "사용 이력 ID", example = "1")
        Long id,
        @Schema(description = "콘티 ID", example = "1")
        Long setlistId,
        @Schema(description = "콘티 제목", example = "2024-01-07 주일예배")
        String setlistTitle,
        @Schema(description = "사용된 키", example = "A")
        String usedKey,
        @Schema(description = "사용 날짜", example = "2024-01-07")
        LocalDate usedAt
) {

    public static SongUsageResponse from(SongUsage usage) {
        return new SongUsageResponse(
                usage.getId(),
                usage.getSetlist().getId(),
                usage.getSetlist().getTitle(),
                usage.getUsedKey(),
                usage.getUsedAt()
        );
    }
}

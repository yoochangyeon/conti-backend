package com.conti.domain.song.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "인기 곡 응답")
public record TopSongResponse(
        @Schema(description = "곡 ID") Long songId,
        @Schema(description = "곡 제목") String title,
        @Schema(description = "아티스트") String artist,
        @Schema(description = "원키") String originalKey,
        @Schema(description = "사용 횟수") long usageCount,
        @Schema(description = "마지막 사용일") LocalDate lastUsedAt
) {}

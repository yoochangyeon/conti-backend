package com.conti.domain.song.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Schema(description = "곡 사용 통계 응답")
public record SongStatsResponse(
        @Schema(description = "곡 ID") Long songId,
        @Schema(description = "곡 제목") String title,
        @Schema(description = "아티스트") String artist,
        @Schema(description = "총 사용 횟수") long totalUsageCount,
        @Schema(description = "마지막 사용일") LocalDate lastUsedAt,
        @Schema(description = "월별 사용 횟수") List<MonthlyUsage> monthlyUsages,
        @Schema(description = "키별 사용 분포") List<KeyUsage> keyDistribution,
        @Schema(description = "인도자별 사용 분포") List<LeaderUsage> leaderBreakdown
) {

    public record MonthlyUsage(
            @Schema(description = "연도") int year,
            @Schema(description = "월") int month,
            @Schema(description = "사용 횟수") long count
    ) {}

    public record KeyUsage(
            @Schema(description = "키") String key,
            @Schema(description = "사용 횟수") long count
    ) {}

    public record LeaderUsage(
            @Schema(description = "인도자 ID") Long leaderId,
            @Schema(description = "인도자 이름") String leaderName,
            @Schema(description = "사용 횟수") long count
    ) {}
}

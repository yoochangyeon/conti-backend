package com.conti.domain.setlist.dto;

import com.conti.domain.setlist.entity.Setlist;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "콘티 응답")
public record SetlistResponse(
        @Schema(description = "콘티 ID", example = "1")
        Long id,
        @Schema(description = "콘티 제목", example = "2024-01-07 주일예배")
        String title,
        @Schema(description = "예배 날짜", example = "2024-01-07")
        LocalDate worshipDate,
        @Schema(description = "예배 타입", example = "SUNDAY_1ST")
        String worshipType,
        @Schema(description = "예배 타입 표시명", example = "주일 1부 예배")
        String worshipTypeDisplayName,
        @Schema(description = "인도자 ID", example = "1")
        Long leaderId,
        @Schema(description = "곡 수", example = "5")
        int songCount,
        @Schema(description = "생성 일시")
        LocalDateTime createdAt
) {

    public static SetlistResponse from(Setlist setlist) {
        return new SetlistResponse(
                setlist.getId(),
                setlist.getTitle(),
                setlist.getWorshipDate(),
                setlist.getWorshipType() != null ? setlist.getWorshipType().name() : null,
                setlist.getWorshipType() != null ? setlist.getWorshipType().getDisplayName() : null,
                setlist.getLeaderId(),
                setlist.getSetlistItems().size(),
                setlist.getCreatedAt()
        );
    }
}

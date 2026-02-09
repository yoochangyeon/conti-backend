package com.conti.domain.setlist.dto;

import com.conti.domain.setlist.entity.Setlist;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "콘티 상세 응답")
public record SetlistDetailResponse(
        @Schema(description = "콘티 ID", example = "1")
        Long id,
        @Schema(description = "콘티 제목", example = "2024-01-07 주일예배")
        String title,
        @Schema(description = "예배 날짜", example = "2024-01-07")
        LocalDate worshipDate,
        @Schema(description = "예배 타입", example = "주일예배")
        String worshipType,
        @Schema(description = "인도자 ID", example = "1")
        Long leaderId,
        @Schema(description = "곡 수", example = "5")
        int songCount,
        @Schema(description = "생성 일시")
        LocalDateTime createdAt,
        @Schema(description = "메모")
        String memo,
        @Schema(description = "생성자 ID", example = "1")
        Long creatorId,
        @Schema(description = "콘티 곡 목록")
        List<SetlistItemResponse> items
) {

    public static SetlistDetailResponse from(Setlist setlist) {
        List<SetlistItemResponse> itemResponses = setlist.getSetlistItems().stream()
                .map(SetlistItemResponse::from)
                .toList();

        return new SetlistDetailResponse(
                setlist.getId(),
                setlist.getTitle(),
                setlist.getWorshipDate(),
                setlist.getWorshipType(),
                setlist.getLeaderId(),
                setlist.getSetlistItems().size(),
                setlist.getCreatedAt(),
                setlist.getMemo(),
                setlist.getCreatorId(),
                itemResponses
        );
    }
}

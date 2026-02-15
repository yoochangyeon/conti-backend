package com.conti.domain.song.dto;

import com.conti.domain.song.entity.SongArrangement;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "편곡 응답")
public record ArrangementResponse(
        @Schema(description = "편곡 ID") Long id,
        @Schema(description = "편곡 이름") String name,
        @Schema(description = "키") String songKey,
        @Schema(description = "BPM") Integer bpm,
        @Schema(description = "박자") String meter,
        @Schema(description = "소요 시간 (분)") Integer durationMinutes,
        @Schema(description = "설명") String description,
        @Schema(description = "기본 편곡 여부") Boolean isDefault,
        @Schema(description = "생성 일시") LocalDateTime createdAt,
        @Schema(description = "섹션 목록") List<SongSectionResponse> sections
) {

    public static ArrangementResponse from(SongArrangement arrangement) {
        List<SongSectionResponse> sectionResponses = arrangement.getSections().stream()
                .map(SongSectionResponse::from)
                .toList();

        return new ArrangementResponse(
                arrangement.getId(),
                arrangement.getName(),
                arrangement.getSongKey(),
                arrangement.getBpm(),
                arrangement.getMeter(),
                arrangement.getDurationMinutes(),
                arrangement.getDescription(),
                arrangement.getIsDefault(),
                arrangement.getCreatedAt(),
                sectionResponses
        );
    }
}

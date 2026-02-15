package com.conti.domain.setlist.dto;

import com.conti.domain.setlist.entity.SetlistNote;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "세트리스트 노트 응답")
public record SetlistNoteResponse(
        @Schema(description = "노트 ID")
        Long id,
        @Schema(description = "세트리스트 ID")
        Long setlistId,
        @Schema(description = "작성자 ID")
        Long authorId,
        @Schema(description = "작성자 이름")
        String authorName,
        @Schema(description = "포지션")
        String position,
        @Schema(description = "내용")
        String content,
        @Schema(description = "생성 일시")
        LocalDateTime createdAt,
        @Schema(description = "수정 일시")
        LocalDateTime updatedAt
) {

    public static SetlistNoteResponse from(SetlistNote note, String authorName) {
        return new SetlistNoteResponse(
                note.getId(),
                note.getSetlistId(),
                note.getAuthorId(),
                authorName,
                note.getPosition(),
                note.getContent(),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }
}

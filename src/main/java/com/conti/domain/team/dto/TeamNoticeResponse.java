package com.conti.domain.team.dto;

import com.conti.domain.team.entity.TeamNotice;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "팀 공지사항 응답")
public record TeamNoticeResponse(
        @Schema(description = "공지 ID")
        Long id,
        @Schema(description = "팀 ID")
        Long teamId,
        @Schema(description = "작성자 ID")
        Long authorId,
        @Schema(description = "작성자 이름")
        String authorName,
        @Schema(description = "제목")
        String title,
        @Schema(description = "내용")
        String content,
        @Schema(description = "고정 여부")
        Boolean isPinned,
        @Schema(description = "생성 일시")
        LocalDateTime createdAt,
        @Schema(description = "수정 일시")
        LocalDateTime updatedAt
) {

    public static TeamNoticeResponse from(TeamNotice notice, String authorName) {
        return new TeamNoticeResponse(
                notice.getId(),
                notice.getTeamId(),
                notice.getAuthorId(),
                authorName,
                notice.getTitle(),
                notice.getContent(),
                notice.getIsPinned(),
                notice.getCreatedAt(),
                notice.getUpdatedAt()
        );
    }
}

package com.conti.domain.team.dto;

import com.conti.domain.team.entity.Team;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "팀 응답")
public record TeamResponse(
        @Schema(description = "팀 ID", example = "1")
        Long id,
        @Schema(description = "팀 이름", example = "사랑의교회 찬양팀")
        String name,
        @Schema(description = "팀 설명", example = "주일예배 찬양팀입니다")
        String description,
        @Schema(description = "초대 코드", example = "abc123")
        String inviteCode,
        @Schema(description = "생성 일시")
        LocalDateTime createdAt
) {

    public static TeamResponse from(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getDescription(),
                team.getInviteCode(),
                team.getCreatedAt()
        );
    }
}

package com.conti.domain.user.dto;

import com.conti.domain.team.entity.TeamMember;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "사용자 팀 정보 응답")
public record UserTeamResponse(
        @Schema(description = "팀 ID", example = "1")
        Long teamId,
        @Schema(description = "팀 이름", example = "사랑의교회 찬양팀")
        String teamName,
        @Schema(description = "역할", example = "ADMIN")
        String role,
        @Schema(description = "가입 일시")
        LocalDateTime joinedAt
) {

    public static UserTeamResponse from(TeamMember teamMember) {
        return new UserTeamResponse(
                teamMember.getTeam().getId(),
                teamMember.getTeam().getName(),
                teamMember.getRole().name(),
                teamMember.getCreatedAt()
        );
    }
}

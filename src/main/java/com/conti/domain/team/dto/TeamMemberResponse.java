package com.conti.domain.team.dto;

import com.conti.domain.team.entity.TeamMember;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "팀 멤버 응답")
public record TeamMemberResponse(
        @Schema(description = "멤버 ID", example = "1")
        Long memberId,
        @Schema(description = "사용자 ID", example = "1")
        Long userId,
        @Schema(description = "사용자 이름", example = "홍길동")
        String userName,
        @Schema(description = "프로필 이미지 URL")
        String profileImage,
        @Schema(description = "역할", example = "ADMIN")
        String role,
        @Schema(description = "가입 일시")
        LocalDateTime joinedAt
) {

    public static TeamMemberResponse from(TeamMember member) {
        return new TeamMemberResponse(
                member.getId(),
                member.getUser().getId(),
                member.getUser().getName(),
                member.getUser().getProfileImage(),
                member.getRole().name(),
                member.getCreatedAt()
        );
    }
}

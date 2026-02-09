package com.conti.domain.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "초대 코드 응답")
public record InviteResponse(
        @Schema(description = "초대 코드", example = "abc123")
        String inviteCode
) {
}

package com.conti.domain.song.controller;

import com.conti.domain.song.dto.TagResponse;
import com.conti.domain.song.service.SongService;
import com.conti.global.auth.TeamAuth;
import com.conti.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "태그", description = "팀 태그 관리")
@RestController
@RequestMapping("/api/v1/teams/{teamId}/tags")
@RequiredArgsConstructor
public class TagController {

    private final SongService songService;

    @Operation(summary = "팀 태그 목록 조회")
    @TeamAuth(roles = {"ADMIN", "VIEWER", "GUEST"})
    @GetMapping
    public ApiResponse<List<TagResponse>> getTags(
            @Parameter(description = "팀 ID") @PathVariable Long teamId) {
        return ApiResponse.ok(songService.getTeamTags(teamId));
    }
}

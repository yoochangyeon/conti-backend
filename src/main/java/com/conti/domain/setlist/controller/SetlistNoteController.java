package com.conti.domain.setlist.controller;

import com.conti.domain.setlist.dto.SetlistNoteCreateRequest;
import com.conti.domain.setlist.dto.SetlistNoteResponse;
import com.conti.domain.setlist.dto.SetlistNoteUpdateRequest;
import com.conti.domain.setlist.service.SetlistNoteService;
import com.conti.global.auth.LoginUser;
import com.conti.global.auth.TeamAuth;
import com.conti.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "세트리스트 노트", description = "세트리스트 포지션별 노트 관리")
@RestController
@RequestMapping("/api/v1/teams/{teamId}/setlists/{setlistId}/notes")
@RequiredArgsConstructor
public class SetlistNoteController {

    private final SetlistNoteService setlistNoteService;

    @Operation(summary = "노트 목록 조회")
    @TeamAuth(roles = {"VIEWER"})
    @GetMapping
    public ApiResponse<List<SetlistNoteResponse>> getNotes(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "세트리스트 ID") @PathVariable Long setlistId,
            @Parameter(description = "포지션 필터") @RequestParam(required = false) String position
    ) {
        return ApiResponse.ok(setlistNoteService.getNotes(setlistId, position));
    }

    @Operation(summary = "노트 생성")
    @TeamAuth(roles = {"EDITOR"})
    @PostMapping
    public ApiResponse<SetlistNoteResponse> createNote(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "세트리스트 ID") @PathVariable Long setlistId,
            @LoginUser Long userId,
            @Valid @RequestBody SetlistNoteCreateRequest request
    ) {
        return ApiResponse.ok(setlistNoteService.createNote(setlistId, userId, request));
    }

    @Operation(summary = "노트 수정")
    @TeamAuth(roles = {"EDITOR"})
    @PatchMapping("/{noteId}")
    public ApiResponse<SetlistNoteResponse> updateNote(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "세트리스트 ID") @PathVariable Long setlistId,
            @Parameter(description = "노트 ID") @PathVariable Long noteId,
            @Valid @RequestBody SetlistNoteUpdateRequest request
    ) {
        return ApiResponse.ok(setlistNoteService.updateNote(noteId, request));
    }

    @Operation(summary = "노트 삭제")
    @TeamAuth(roles = {"EDITOR"})
    @DeleteMapping("/{noteId}")
    public ApiResponse<Void> deleteNote(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "세트리스트 ID") @PathVariable Long setlistId,
            @Parameter(description = "노트 ID") @PathVariable Long noteId
    ) {
        setlistNoteService.deleteNote(noteId);
        return ApiResponse.ok();
    }
}

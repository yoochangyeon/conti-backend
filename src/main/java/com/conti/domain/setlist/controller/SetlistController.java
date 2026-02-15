package com.conti.domain.setlist.controller;

import com.conti.domain.setlist.dto.ReorderRequest;
import com.conti.domain.setlist.dto.SetlistCopyRequest;
import com.conti.domain.setlist.dto.SetlistCreateRequest;
import com.conti.domain.setlist.dto.SetlistDetailResponse;
import com.conti.domain.setlist.dto.SetlistItemRequest;
import com.conti.domain.setlist.dto.SetlistItemResponse;
import com.conti.domain.setlist.dto.SetlistResponse;
import com.conti.domain.setlist.dto.SetlistSearchCondition;
import com.conti.domain.setlist.dto.SetlistUpdateRequest;
import com.conti.domain.setlist.entity.WorshipType;
import com.conti.domain.setlist.service.SetlistService;
import com.conti.global.auth.LoginUser;
import com.conti.global.auth.TeamAuth;
import com.conti.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "콘티", description = "콘티(세트리스트) 관리")
@RestController
@RequestMapping("/api/v1/teams/{teamId}/setlists")
@RequiredArgsConstructor
public class SetlistController {

    private final SetlistService setlistService;

    @Operation(summary = "콘티 목록 조회")
    @TeamAuth(roles = {"ADMIN", "VIEWER", "GUEST"})
    @GetMapping
    public ApiResponse<Page<SetlistResponse>> getSetlists(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "시작 날짜 (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "종료 날짜 (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "예배 타입 필터") @RequestParam(required = false) WorshipType worshipType,
            Pageable pageable
    ) {
        SetlistSearchCondition condition = new SetlistSearchCondition(fromDate, toDate, worshipType);
        return ApiResponse.ok(setlistService.getSetlists(teamId, condition, pageable));
    }

    @Operation(summary = "콘티 생성")
    @TeamAuth(roles = {"ADMIN"})
    @PostMapping
    public ApiResponse<SetlistResponse> createSetlist(
            @LoginUser Long userId,
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "템플릿 ID (선택)") @RequestParam(required = false) Long templateId,
            @Valid @RequestBody SetlistCreateRequest request
    ) {
        if (templateId != null) {
            return ApiResponse.ok(setlistService.createSetlistFromTemplate(teamId, userId, request, templateId));
        }
        return ApiResponse.ok(setlistService.createSetlist(teamId, userId, request));
    }

    @Operation(summary = "콘티 상세 조회")
    @TeamAuth(roles = {"ADMIN", "VIEWER", "GUEST"})
    @GetMapping("/{setlistId}")
    public ApiResponse<SetlistDetailResponse> getSetlist(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "콘티 ID") @PathVariable Long setlistId
    ) {
        return ApiResponse.ok(setlistService.getSetlist(setlistId));
    }

    @Operation(summary = "콘티 수정")
    @TeamAuth(roles = {"ADMIN"})
    @PatchMapping("/{setlistId}")
    public ApiResponse<SetlistResponse> updateSetlist(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "콘티 ID") @PathVariable Long setlistId,
            @RequestBody SetlistUpdateRequest request
    ) {
        return ApiResponse.ok(setlistService.updateSetlist(setlistId, request));
    }

    @Operation(summary = "콘티 삭제")
    @TeamAuth(roles = {"ADMIN"})
    @DeleteMapping("/{setlistId}")
    public ApiResponse<Void> deleteSetlist(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "콘티 ID") @PathVariable Long setlistId
    ) {
        setlistService.deleteSetlist(setlistId);
        return ApiResponse.ok();
    }

    @Operation(summary = "콘티 복사")
    @TeamAuth(roles = {"ADMIN"})
    @PostMapping("/{setlistId}/copy")
    public ApiResponse<SetlistResponse> copySetlist(
            @LoginUser Long userId,
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "콘티 ID") @PathVariable Long setlistId,
            @Valid @RequestBody SetlistCopyRequest request
    ) {
        return ApiResponse.ok(setlistService.copySetlist(setlistId, userId, request));
    }

    @Operation(summary = "콘티에 곡 추가")
    @TeamAuth(roles = {"ADMIN"})
    @PostMapping("/{setlistId}/items")
    public ApiResponse<SetlistItemResponse> addItem(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "콘티 ID") @PathVariable Long setlistId,
            @Valid @RequestBody SetlistItemRequest request
    ) {
        return ApiResponse.ok(setlistService.addItem(setlistId, request));
    }

    @Operation(summary = "콘티 곡 수정")
    @TeamAuth(roles = {"ADMIN"})
    @PatchMapping("/{setlistId}/items/{itemId}")
    public ApiResponse<SetlistItemResponse> updateItem(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "콘티 ID") @PathVariable Long setlistId,
            @Parameter(description = "콘티 아이템 ID") @PathVariable Long itemId,
            @RequestBody SetlistItemRequest request
    ) {
        return ApiResponse.ok(setlistService.updateItem(setlistId, itemId, request));
    }

    @Operation(summary = "콘티 곡 제거")
    @TeamAuth(roles = {"ADMIN"})
    @DeleteMapping("/{setlistId}/items/{itemId}")
    public ApiResponse<Void> removeItem(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "콘티 ID") @PathVariable Long setlistId,
            @Parameter(description = "콘티 아이템 ID") @PathVariable Long itemId
    ) {
        setlistService.removeItem(setlistId, itemId);
        return ApiResponse.ok();
    }

    @Operation(summary = "콘티 곡 순서 변경")
    @TeamAuth(roles = {"ADMIN"})
    @PatchMapping("/{setlistId}/items/reorder")
    public ApiResponse<Void> reorderItems(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "콘티 ID") @PathVariable Long setlistId,
            @RequestBody ReorderRequest request
    ) {
        setlistService.reorderItems(setlistId, request);
        return ApiResponse.ok();
    }
}

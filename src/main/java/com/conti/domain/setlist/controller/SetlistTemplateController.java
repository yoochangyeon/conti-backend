package com.conti.domain.setlist.controller;

import com.conti.domain.setlist.dto.SetlistTemplateCreateRequest;
import com.conti.domain.setlist.dto.SetlistTemplateResponse;
import com.conti.domain.setlist.service.SetlistTemplateService;
import com.conti.global.auth.TeamAuth;
import com.conti.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "콘티 템플릿", description = "콘티 템플릿 관리")
@RestController
@RequestMapping("/api/v1/teams/{teamId}/setlist-templates")
@RequiredArgsConstructor
public class SetlistTemplateController {

    private final SetlistTemplateService templateService;

    @Operation(summary = "템플릿 목록 조회")
    @TeamAuth(roles = {"ADMIN", "VIEWER", "GUEST"})
    @GetMapping
    public ApiResponse<List<SetlistTemplateResponse>> getTemplates(
            @Parameter(description = "팀 ID") @PathVariable Long teamId
    ) {
        return ApiResponse.ok(templateService.getTemplates(teamId));
    }

    @Operation(summary = "템플릿 상세 조회")
    @TeamAuth(roles = {"ADMIN", "VIEWER", "GUEST"})
    @GetMapping("/{templateId}")
    public ApiResponse<SetlistTemplateResponse> getTemplate(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "템플릿 ID") @PathVariable Long templateId
    ) {
        return ApiResponse.ok(templateService.getTemplate(templateId));
    }

    @Operation(summary = "템플릿 생성")
    @TeamAuth(roles = {"ADMIN"})
    @PostMapping
    public ApiResponse<SetlistTemplateResponse> createTemplate(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Valid @RequestBody SetlistTemplateCreateRequest request
    ) {
        return ApiResponse.ok(templateService.createTemplate(teamId, request));
    }

    @Operation(summary = "템플릿 수정")
    @TeamAuth(roles = {"ADMIN"})
    @PutMapping("/{templateId}")
    public ApiResponse<SetlistTemplateResponse> updateTemplate(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "템플릿 ID") @PathVariable Long templateId,
            @Valid @RequestBody SetlistTemplateCreateRequest request
    ) {
        return ApiResponse.ok(templateService.updateTemplate(templateId, request));
    }

    @Operation(summary = "템플릿 삭제")
    @TeamAuth(roles = {"ADMIN"})
    @DeleteMapping("/{templateId}")
    public ApiResponse<Void> deleteTemplate(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "템플릿 ID") @PathVariable Long templateId
    ) {
        templateService.deleteTemplate(templateId);
        return ApiResponse.ok();
    }
}

package com.conti.domain.schedule.controller;

import com.conti.domain.schedule.dto.EnumResponse;
import com.conti.domain.setlist.entity.SetlistItemType;
import com.conti.domain.setlist.entity.WorshipType;
import com.conti.domain.team.entity.Position;
import com.conti.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@Tag(name = "Enum", description = "열거형 값 조회")
@RestController
@RequestMapping("/api/v1/enums")
public class EnumController {

    @Operation(summary = "예배 타입 목록")
    @GetMapping("/worship-types")
    public ApiResponse<List<EnumResponse>> getWorshipTypes() {
        List<EnumResponse> types = Arrays.stream(WorshipType.values())
                .map(wt -> new EnumResponse(wt.name(), wt.getDisplayName()))
                .toList();
        return ApiResponse.ok(types);
    }

    @Operation(summary = "포지션 목록")
    @GetMapping("/positions")
    public ApiResponse<List<EnumResponse>> getPositions() {
        List<EnumResponse> positions = Arrays.stream(Position.values())
                .map(p -> new EnumResponse(p.name(), p.getDisplayName()))
                .toList();
        return ApiResponse.ok(positions);
    }

    @Operation(summary = "콘티 항목 타입 목록")
    @GetMapping("/setlist-item-types")
    public ApiResponse<List<EnumResponse>> getSetlistItemTypes() {
        List<EnumResponse> types = Arrays.stream(SetlistItemType.values())
                .map(t -> new EnumResponse(t.name(), t.getDisplayName()))
                .toList();
        return ApiResponse.ok(types);
    }
}

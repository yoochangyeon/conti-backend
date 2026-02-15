package com.conti.domain.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "스케줄 매트릭스 응답")
public record ScheduleMatrixResponse(
        @Schema(description = "포지션 목록 (행)")
        List<String> positions,
        @Schema(description = "포지션 표시명 목록")
        List<String> positionDisplayNames,
        @Schema(description = "날짜 목록 (열)")
        List<LocalDate> dates,
        @Schema(description = "콘티 정보 (날짜별)")
        List<DateSetlistInfo> dateSetlists,
        @Schema(description = "매트릭스 셀 데이터")
        List<MatrixCell> cells
) {

    @Schema(description = "날짜별 콘티 정보")
    public record DateSetlistInfo(
            @Schema(description = "날짜") LocalDate date,
            @Schema(description = "콘티 ID") Long setlistId,
            @Schema(description = "예배 유형") String worshipType,
            @Schema(description = "예배 유형 표시명") String worshipTypeDisplayName,
            @Schema(description = "콘티 제목") String title
    ) {}

    @Schema(description = "매트릭스 셀")
    public record MatrixCell(
            @Schema(description = "날짜") LocalDate date,
            @Schema(description = "포지션") String position,
            @Schema(description = "배정된 멤버 목록") List<CellMember> members
    ) {}

    @Schema(description = "셀 내 멤버")
    public record CellMember(
            @Schema(description = "스케줄 ID") Long scheduleId,
            @Schema(description = "팀 멤버 ID") Long teamMemberId,
            @Schema(description = "멤버 이름") String memberName,
            @Schema(description = "프로필 이미지") String profileImage,
            @Schema(description = "상태") String status,
            @Schema(description = "상태 표시명") String statusDisplayName
    ) {}
}

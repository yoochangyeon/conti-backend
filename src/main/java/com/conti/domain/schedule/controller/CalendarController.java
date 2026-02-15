package com.conti.domain.schedule.controller;

import com.conti.domain.schedule.service.CalendarService;
import com.conti.global.auth.LoginUser;
import com.conti.global.auth.jwt.JwtTokenProvider;
import com.conti.global.common.ApiResponse;
import com.conti.global.error.BusinessException;
import com.conti.global.error.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "캘린더", description = "iCal 캘린더 피드")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "캘린더 토큰 발급", description = "iCal 피드 구독용 장기 토큰을 발급합니다")
    @GetMapping("/users/me/calendar-token")
    public ApiResponse<String> getCalendarToken(@LoginUser Long userId) {
        String token = jwtTokenProvider.generateCalendarToken(userId);
        return ApiResponse.ok(token);
    }

    @Operation(summary = "iCal 피드", description = "캘린더 앱에서 구독할 수 있는 .ics 피드")
    @GetMapping(value = "/calendar.ics", produces = "text/calendar")
    public ResponseEntity<String> getCalendarFeed(@RequestParam("token") String token) {
        if (!jwtTokenProvider.validateToken(token) || !jwtTokenProvider.isCalendarToken(token)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        Long userId = jwtTokenProvider.getUserId(token);
        String icsContent = calendarService.generateICalFeed(userId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/calendar; charset=UTF-8"))
                .header("Content-Disposition", "inline; filename=\"conti-schedule.ics\"")
                .body(icsContent);
    }
}

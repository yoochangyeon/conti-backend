package com.conti.domain.notification.controller;

import com.conti.domain.notification.dto.DeviceTokenRequest;
import com.conti.domain.notification.dto.NotificationResponse;
import com.conti.domain.notification.dto.UnreadCountResponse;
import com.conti.domain.notification.service.NotificationService;
import com.conti.global.auth.LoginUser;
import com.conti.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "알림", description = "알림 관리")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "디바이스 토큰 등록")
    @PostMapping("/device-token")
    public ApiResponse<Void> registerDeviceToken(
            @LoginUser Long userId,
            @Valid @RequestBody DeviceTokenRequest request
    ) {
        notificationService.registerDeviceToken(userId, request);
        return ApiResponse.ok();
    }

    @Operation(summary = "알림 목록 조회")
    @GetMapping
    public ApiResponse<Page<NotificationResponse>> getNotifications(
            @LoginUser Long userId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.ok(notificationService.getNotifications(userId, pageable));
    }

    @Operation(summary = "알림 읽음 처리")
    @PatchMapping("/{id}/read")
    public ApiResponse<Void> markAsRead(
            @LoginUser Long userId,
            @PathVariable Long id
    ) {
        notificationService.markAsRead(id, userId);
        return ApiResponse.ok();
    }

    @Operation(summary = "전체 알림 읽음 처리")
    @PatchMapping("/read-all")
    public ApiResponse<Void> markAllAsRead(@LoginUser Long userId) {
        notificationService.markAllAsRead(userId);
        return ApiResponse.ok();
    }

    @Operation(summary = "읽지 않은 알림 수 조회")
    @GetMapping("/unread-count")
    public ApiResponse<UnreadCountResponse> getUnreadCount(@LoginUser Long userId) {
        return ApiResponse.ok(new UnreadCountResponse(notificationService.getUnreadCount(userId)));
    }
}

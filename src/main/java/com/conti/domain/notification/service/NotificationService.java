package com.conti.domain.notification.service;

import com.conti.domain.notification.dto.DeviceTokenRequest;
import com.conti.domain.notification.dto.NotificationResponse;
import com.conti.domain.notification.entity.DeviceToken;
import com.conti.domain.notification.entity.Notification;
import com.conti.domain.notification.entity.NotificationType;
import com.conti.domain.notification.repository.DeviceTokenRepository;
import com.conti.domain.notification.repository.NotificationRepository;
import com.conti.global.error.BusinessException;
import com.conti.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final PushNotificationService pushNotificationService;

    @Transactional
    public void registerDeviceToken(Long userId, DeviceTokenRequest request) {
        deviceTokenRepository.findByFcmToken(request.fcmToken())
                .ifPresentOrElse(
                        token -> token.updateToken(request.fcmToken(), request.platform()),
                        () -> deviceTokenRepository.save(DeviceToken.builder()
                                .userId(userId)
                                .fcmToken(request.fcmToken())
                                .platform(request.platform())
                                .build())
                );
    }

    @Transactional
    public void removeDeviceToken(String fcmToken) {
        deviceTokenRepository.deleteByFcmToken(fcmToken);
    }

    public Page<NotificationResponse> getNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(NotificationResponse::from);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        notification.markAsRead();
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void createNotification(Long userId, NotificationType type, String title, String message,
                                   String referenceType, Long referenceId) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .build();

        notificationRepository.save(notification);
        pushNotificationService.sendPush(userId, notification);
    }
}

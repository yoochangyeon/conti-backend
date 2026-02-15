package com.conti.domain.notification.service;

import com.conti.domain.notification.entity.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MockPushNotificationService implements PushNotificationService {

    @Override
    public void sendPush(Long userId, Notification notification) {
        log.info("[MOCK PUSH] userId={}, type={}, title={}, message={}",
                userId, notification.getType(), notification.getTitle(), notification.getMessage());
    }
}

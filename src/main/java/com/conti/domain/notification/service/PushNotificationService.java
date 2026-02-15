package com.conti.domain.notification.service;

import com.conti.domain.notification.entity.Notification;

public interface PushNotificationService {

    void sendPush(Long userId, Notification notification);
}

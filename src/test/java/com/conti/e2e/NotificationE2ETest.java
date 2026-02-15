package com.conti.e2e;

import com.conti.domain.notification.dto.DeviceTokenRequest;
import com.conti.domain.notification.entity.Notification;
import com.conti.domain.notification.entity.NotificationType;
import com.conti.domain.notification.repository.NotificationRepository;
import com.conti.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("알림 E2E 테스트")
class NotificationE2ETest extends BaseE2ETest {

    @Autowired
    private NotificationRepository notificationRepository;

    private User user;
    private String token;

    @BeforeEach
    void setUp() {
        user = createUser("noti-user@test.com", "알림 테스트 유저");
        token = getToken(user.getId());
    }

    private Notification createNotification(Long userId, String title, String message, boolean isRead) {
        return notificationRepository.save(Notification.builder()
                .userId(userId)
                .type(NotificationType.SCHEDULE_ASSIGNED)
                .title(title)
                .message(message)
                .referenceType("SETLIST")
                .referenceId(1L)
                .isRead(isRead)
                .build());
    }

    @Nested
    @DisplayName("디바이스 토큰 등록")
    class RegisterDeviceToken {

        @Test
        @DisplayName("디바이스 토큰을 등록한다")
        void registerToken() throws Exception {
            DeviceTokenRequest request = new DeviceTokenRequest("test-fcm-token-12345", "ANDROID");

            performPost("/api/v1/notifications/device-token", token, request)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("FCM 토큰 없이 등록 시 400 에러를 반환한다")
        void registerWithoutTokenReturns400() throws Exception {
            DeviceTokenRequest request = new DeviceTokenRequest(null, "ANDROID");

            performPost("/api/v1/notifications/device-token", token, request)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("플랫폼 없이 등록 시 400 에러를 반환한다")
        void registerWithoutPlatformReturns400() throws Exception {
            DeviceTokenRequest request = new DeviceTokenRequest("test-token", null);

            performPost("/api/v1/notifications/device-token", token, request)
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("알림 목록 조회")
    class GetNotifications {

        @Test
        @DisplayName("알림 목록을 페이징하여 조회한다")
        void getNotificationsWithPaging() throws Exception {
            for (int i = 1; i <= 3; i++) {
                createNotification(user.getId(), "알림 " + i, "메시지 " + i, false);
            }

            performGet("/api/v1/notifications?page=0&size=20", token)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content", hasSize(3)))
                    .andExpect(jsonPath("$.data.totalElements").value(3));
        }

        @Test
        @DisplayName("알림이 없으면 빈 목록을 반환한다")
        void getEmptyNotifications() throws Exception {
            performGet("/api/v1/notifications?page=0&size=20", token)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("알림 읽음 처리")
    class MarkAsRead {

        @Test
        @DisplayName("개별 알림을 읽음 처리한다")
        void markSingleAsRead() throws Exception {
            Notification notification = createNotification(user.getId(), "읽을 알림", "메시지", false);

            performPatchNoBody("/api/v1/notifications/" + notification.getId() + "/read", token)
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("전체 알림을 읽음 처리한다")
        void markAllNotificationsAsRead() throws Exception {
            createNotification(user.getId(), "알림 1", "메시지 1", false);
            createNotification(user.getId(), "알림 2", "메시지 2", false);

            performPatchNoBody("/api/v1/notifications/read-all", token)
                    .andExpect(status().isOk());

            // 읽지 않은 알림 수 확인
            performGet("/api/v1/notifications/unread-count", token)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.count").value(0));
        }
    }

    @Nested
    @DisplayName("읽지 않은 알림 수 조회")
    class UnreadCount {

        @Test
        @DisplayName("읽지 않은 알림 수를 조회한다")
        void getUnreadCount() throws Exception {
            createNotification(user.getId(), "읽지 않은 알림 1", "메시지", false);
            createNotification(user.getId(), "읽지 않은 알림 2", "메시지", false);
            createNotification(user.getId(), "읽은 알림", "메시지", true);

            performGet("/api/v1/notifications/unread-count", token)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.count").value(2));
        }
    }
}

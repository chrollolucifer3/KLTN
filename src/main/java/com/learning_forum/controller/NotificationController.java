package com.learning_forum.controller;

import com.learning_forum.dto.request.ApiResponse;
import com.learning_forum.dto.request.ReadNotificationRequest;
import com.learning_forum.dto.respone.ListNotificationResponse;
import com.learning_forum.service.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("notification")
public class NotificationController {

    NotificationService notificationService;

    //Api lấy danh sách thông báo của người dùng
    @GetMapping("/list")
    public ApiResponse<ListNotificationResponse> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String order
    ) {
        log.info("Fetching notifications for user");

        return ApiResponse.<ListNotificationResponse>builder()
                .code(200)
                .message("Success")
                .result(notificationService.getAllNotifications(page, size, sortBy, order))
                .build();
    }

    //Api lấy số lượng thông báo chưa đọc của người dùng
    @GetMapping("/unread")
    public ApiResponse<Integer> getUnreadNotificationCount() {
        log.info("Fetching unread notification count for user");

        int unreadCount = notificationService.getUnreadNotificationCount();
        return ApiResponse.<Integer>builder()
                .code(200)
                .message("Success")
                .result(unreadCount)
                .build();
    }

    //Api đánh dấu thong báo là đã đọc
    @PostMapping("/markAsRead")
    public ApiResponse<ReadNotificationRequest> markNotificationAsRead(@RequestBody ReadNotificationRequest request) {
        log.info("Marking notification as read: {}", request.getNotificationId());

        notificationService.markNotificationAsRead(request.getNotificationId());
        return ApiResponse.<ReadNotificationRequest>builder()
                .code(200)
                .message("Notification marked as read")
                .build();
    }

    //Api đánh dấu tất cả thông báo là đã đọc
    @PostMapping("/markAllAsRead")
    public ApiResponse<Void> markAllNotificationsAsRead() {
        log.info("Marking all notifications as read");

        notificationService.markAllNotificationsAsRead();
        return ApiResponse.<Void>builder()
                .code(200)
                .message("All notifications marked as read")
                .build();
    }
}

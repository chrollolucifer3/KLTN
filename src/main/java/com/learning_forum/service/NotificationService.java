package com.learning_forum.service;

import com.learning_forum.config.SecurityConfig;
import com.learning_forum.domain.NOTIFICATION_TYPE;
import com.learning_forum.dto.respone.ListNotificationResponse;
import com.learning_forum.dto.respone.NotificationResponse;
import com.learning_forum.dto.respone.PostResponse;
import com.learning_forum.dto.request.NotificationMessage;
import com.learning_forum.entity.Notification;
import com.learning_forum.entity.Post;
import com.learning_forum.entity.User;
import com.learning_forum.exception.AppException;
import com.learning_forum.exception.ErrorCode;
import com.learning_forum.repository.FollowRepository;
import com.learning_forum.repository.NotificationRepository;
import com.learning_forum.repository.PostRepository;
import com.learning_forum.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationService {

    SimpMessagingTemplate messagingTemplate;
    FollowRepository followRepository;
    NotificationRepository notificationRepository;
    PostRepository postRepository;
    UserRepository userRepository;
    SecurityConfig securityConfig;

    /**
     * Gửi thông báo realtime đến followers khi tác giả đăng bài mới
     *
     * @param authorId ID tác giả (String UUID hoặc Long tùy hệ thống bạn)
     * @param post     Bài viết mới tạo
     */
    public void notifyFollowersNewPost(String authorId, PostResponse post) {
        List<User> followers = followRepository.findFollowersByUserId(authorId);

        if (followers.isEmpty()) {
            log.info("Không có follower nào cho tác giả {}", authorId);
            return;
        }
        var authorName = post.getAuthorName();
        for (User follower : followers) {
            NotificationMessage message = NotificationMessage.builder()
                    .type(NOTIFICATION_TYPE.NEW_POST)
                    .title("Tác giả" + authorName + " vừa đăng bài mới!")
                    .content(post.getTitle())
                    .postId(post.getId())
                    .authorName(post.getAuthorName())
                    .createdAt(LocalDateTime.now())
                    .build();

            // Lưu thông báo vào cơ sở dữ liệu
            notificationRepository.save(
                    Notification.builder()
                            .type(NOTIFICATION_TYPE.NEW_POST)
                            .title("Tác giả bạn theo dõi vừa đăng bài mới!")
                            .content(post.getTitle())
                            .postId(post.getId())
                            .authorName(post.getAuthorName())
                            .createdAt(LocalDateTime.now())
                            .recipient(follower)
                            .isRead(false) // Mặc định là chưa đọc
                            .build()
            );

            messagingTemplate.convertAndSendToUser(
                    follower.getUsername(),     //  Username phải khớp với tên user bên frontend connect
                    "/queue/notifications",
                    message
            );
        }
    }

    //gửi thông báo đến tác giả khi có người thích bài viết
    public void notifyAuthorPostLiked(String postId, String userId) {

        // Lấy thông tin bài viết và tác giả
        Post post = postRepository.findPostById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        var authorName = post.getUser().getFullName();
        NotificationMessage message = NotificationMessage.builder()
                .type(NOTIFICATION_TYPE.POST_LIKED)
                .title("Bài viết của bạn vừa được thích!")
                .content("Người dùng " + authorName + " đã thích bài viết của bạn.")
                .postId(postId)
                .authorName(authorName)
                .createdAt(LocalDateTime.now())
                .build();

        // Lưu thông báo vào cơ sở dữ liệu
        notificationRepository.save(
                Notification.builder()
                        .type(NOTIFICATION_TYPE.POST_LIKED)
                        .title("Bài viết của bạn vừa được thích!")
                        .content("Người dùng " + authorName + " đã thích bài viết của bạn.")
                        .postId(postId)
                        .authorName(authorName)
                        .createdAt(LocalDateTime.now())
                        .recipient(post.getUser()) // Giả sử User có constructor nhận tên
                        .isRead(false)
                        .build()
        );

        messagingTemplate.convertAndSendToUser(
                post.getUser().getUsername(),     //  Username phải khớp với tên user bên frontend connect
                "/queue/notifications",
                message
        );
        log.info("Đã gửi thông báo đến tác giả bài viết {} với nội dung: {}", postId, message.getContent());
    }

    //Gửi thông báo đến người dùng khi có người bình luận bài viết của họ
    public void notifyAuthorPostCommented(String postId, String userId) {
        // Lấy thông tin bài viết và tác giả
        Post post = postRepository.findPostById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        User commenter = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String commenterName = commenter.getFullName();

        NotificationMessage message = NotificationMessage.builder()
                .type(NOTIFICATION_TYPE.POST_COMMENTED)
                .title("Bài viết của bạn vừa có bình luận mới!")
                .content("Người dùng " + commenterName + " đã bình luận bài viết của bạn.")
                .postId(postId)
                .authorName(commenterName)
                .createdAt(LocalDateTime.now())
                .build();

        // Lưu thông báo vào cơ sở dữ liệu
        notificationRepository.save(
                Notification.builder()
                        .type(NOTIFICATION_TYPE.POST_COMMENTED)
                        .title("Bài viết của bạn vừa có bình luận mới!")
                        .content("Người dùng " + commenterName + " đã bình luận bài viết của bạn.")
                        .postId(postId)
                        .authorName(commenterName)
                        .createdAt(LocalDateTime.now())
                        .recipient(post.getUser()) // Giả sử User có constructor nhận tên
                        .isRead(false)
                        .build()
        );

        messagingTemplate.convertAndSendToUser(
                post.getUser().getUsername(),     //  Username phải khớp với tên user bên frontend connect
                "/queue/notifications",
                message
        );
        log.info("Đã gửi thông báo đến tác giả bài viết {} với nội dung: {}", postId, message.getContent());
    }

    // Lấy danh sách thông báo của người dùng
    public ListNotificationResponse getAllNotifications(int page, int size, String sortBy, String order) {
        String currentUsername = securityConfig.getCurrentUsername();

        int pageIndex = Math.max(page - 1, 0);
        Sort.Direction sortDirection = Sort.Direction.fromString(order);

        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(sortDirection, sortBy));

        Page<Notification> notificationPage = notificationRepository.findByRecipientUsername(currentUsername, pageable);

        List<NotificationResponse> notificationResponses = notificationPage.getContent().stream()
                .map(n -> NotificationResponse.builder()
                        .id(n.getId())
                        .type(n.getType())
                        .title(n.getTitle())
                        .content(n.getContent())
                        .postId(n.getPostId())
                        .authorName(n.getAuthorName())
                        .createdAt(n.getCreatedAt())
                        .isRead(n.getIsRead())
                        .build())
                .toList();

        return ListNotificationResponse.builder()
                .notifications(notificationResponses)
                .total(notificationPage.getTotalElements())
                .totalPages(notificationPage.getTotalPages())
                .page(page)
                .size(size)
                .build();
    }

    // Lấy số lượng thông báo chưa đọc của người dùng
    public int getUnreadNotificationCount() {
        String currentUsername = securityConfig.getCurrentUsername();
        return notificationRepository.countByRecipientUsernameAndIsRead(currentUsername, false);
    }

    // Đánh dấu thông báo là đã đọc
    public void markNotificationAsRead(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (notification.getIsRead()) {
            log.warn("Thông báo đã được đánh dấu là đã đọc trước đó: {}", notificationId);
            return;
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);

        log.info("Đã đánh dấu thông báo {} là đã đọc", notificationId);
    }

    // Đánh dấu tất cả thông báo là đã đọc
    public void markAllNotificationsAsRead() {
        String currentUsername = securityConfig.getCurrentUsername();
        List<Notification> notifications = notificationRepository.findByRecipientUsernameAndIsRead(currentUsername, false);

        if (notifications.isEmpty()) {
            log.info("Không có thông báo chưa đọc để đánh dấu là đã đọc cho người dùng: {}", currentUsername);
            return;
        }

        notifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(notifications);

        log.info("Đã đánh dấu tất cả thông báo là đã đọc cho người dùng: {}", currentUsername);
    }
}

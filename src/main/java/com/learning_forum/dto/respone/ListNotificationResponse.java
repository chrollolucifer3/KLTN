package com.learning_forum.dto.respone;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListNotificationResponse {
    List<NotificationResponse> notifications;
    long total;
    int totalPages;
    int page;
    int size;
}

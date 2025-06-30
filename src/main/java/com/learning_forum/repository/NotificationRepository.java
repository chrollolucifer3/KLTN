package com.learning_forum.repository;

import com.learning_forum.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
    @Query("SELECT n FROM Notification n WHERE n.recipient.username = :username")
    Page<Notification> findByRecipientUsername(@Param("username") String username, Pageable pageable);
    int countByRecipientUsernameAndIsRead(String username, boolean isRead);
    List<Notification> findByRecipientUsernameAndIsRead(String username, boolean isRead);

}

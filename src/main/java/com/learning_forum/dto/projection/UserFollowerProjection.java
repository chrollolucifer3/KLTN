package com.learning_forum.dto.projection;

import java.time.LocalDate;

public interface UserFollowerProjection {
    String getId();
    String getUsername();
    String getEmail();
    String getPhone();
    String getFullName();
    LocalDate getDob();
    String getRole();
    String getAvatarUrl();
    Boolean getIsActive();
    LocalDate getCreatedAt();
    Integer getPostCount();
    Integer getFollowersCount ();
}

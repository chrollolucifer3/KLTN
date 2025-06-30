package com.learning_forum.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum NOTIFICATION_TYPE {
    NEW_POST,
    POST_LIKED,
    POST_COMMENTED;

    @JsonCreator
    public static NOTIFICATION_TYPE fromString(String value) {
        for (NOTIFICATION_TYPE notificationType : NOTIFICATION_TYPE.values()) {
            if (notificationType.name().equalsIgnoreCase(value)) {  // Chấp nhận chữ hoa/thường
                return notificationType;
            }
        }
        throw new IllegalArgumentException("Invalid notificationType: " + value);
    }
}

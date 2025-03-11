package com.learning_forum.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum USER_ROLE {
    ADMIN, SUPER_ADMIN, USER;

    @JsonCreator
    public static USER_ROLE fromString(String value) {
        for (USER_ROLE role : USER_ROLE.values()) {
            if (role.name().equalsIgnoreCase(value)) {  // Chấp nhận chữ hoa/thường
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role: " + value);
    }
}

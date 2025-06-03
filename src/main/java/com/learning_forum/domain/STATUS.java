package com.learning_forum.domain;

public enum STATUS {
    PENDING, APPROVED, REJECTED, BLOCKED;

    public static STATUS fromString(String value) {
        for (STATUS status : STATUS.values()) {
            if (status.name().equalsIgnoreCase(value)) {  // Chấp nhận chữ hoa/thường
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid status: " + value);
    }
}

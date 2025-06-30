package com.learning_forum.service;

public interface EmailService {
    void sendResetPasswordEmail(String to, String resetLink);
}

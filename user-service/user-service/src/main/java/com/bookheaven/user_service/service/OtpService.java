package com.bookheaven.user_service.service;

import jakarta.mail.MessagingException;

public interface OtpService {
    public String generateOtp(String email);
    public void sendOtp(String email) throws MessagingException;
    public void validateOtp(String email, String otp);
}

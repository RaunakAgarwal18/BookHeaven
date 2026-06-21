package com.bookheaven.user_service.constant;

public final class AppConstants {

    private AppConstants() {
        // Prevent instantiation
    }

    // Redis Keys
    public static final String REDIS_KEY_REFRESH = "refresh:";
    public static final String REDIS_KEY_OTP = "otp:";
    public static final String REDIS_KEY_OTP_COOLDOWN = "otp_cooldown:";
    public static final String REDIS_KEY_OTP_COUNT = "otp_count:";
    public static final String REDIS_KEY_EMAIL_TOKEN = "email_token:";
    public static final String REDIS_KEY_SIGNUP = "signup:";
    public static final String RESET_TOKEN_PREFIX = "password_reset:";

    // RabbitMQ Exchanges & Routing Keys
    public static final String EXCHANGE_EMAIL = "email.exchange";
    public static final String ROUTING_KEY_CONTACT = "contact.key";
    public static final String ROUTING_KEY_WELCOME = "email.welcome";
    public static final String ROUTING_KEY_PASSWORD_RESET = "email.password_reset";
    public static final String ROUTING_KEY_OTP = "email.otp";

    // Timeouts and Expirations
    public static final int OTP_EXPIRY_MINUTES = 5;
    public static final int OTP_COOLDOWN_MINUTES = 1;
    public static final int OTP_MAX_ATTEMPTS_MINUTES = 15;
    public static final int SIGNUP_EXPIRY_MINUTES = 30;
    public static final int REFRESH_TOKEN_EXPIRY_MINUTES = 60 * 24 * 15; // 15 Days
    public static final int REFRESH_TOKEN_EXPIRY_DAYS_SHORT = 60 * 24 * 7; // 7 Days
}

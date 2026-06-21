package com.bookheaven.email_service.constants;

public class EmailConstant {

    public static final String OTP_MAIL_SUBJECT = "BookHeaven - OTP for signup";
    public static final String PASSWORD_RESET_MAIL_SUBJECT = "BookHeaven - Password Reset Request";
    public static final String ORDER_CONFIRM_MAIL_SUBJECT = "BookHeaven - Order Confirmed";
    public static final String PAYMENT_FAILED_MAIL_SUBJECT = "BookHeaven - Payment Failed";
    public static final String WELCOME_MAIL_SUBJECT = "BookHeaven - Welcome";

    public static final String EMAIL_EXCHANGE = "email.exchange";
    public static final String ORDER_CONFIRMED_QUEUE = "email.order.confirmed.queue";
    public static final String PAYMENT_FAILED_QUEUE = "email.payment.failed.queue";
    public static final String ORDER_CONFIRMED_KEY = "email.order.confirmed";
    public static final String PAYMENT_FAILED_KEY = "email.payment.failed";
    public static final String DLQ = "email.dlq";
    public static final String DLX = "email.dlx";
    public static final String DLQ_ROUTING_KEY = "email.dead";
    public static final String WELCOME_QUEUE = "email.welcome.queue";
    public static final String WELCOME_KEY = "email.user.welcome";

    public static final String SELLER_ORDER_QUEUE = "email.seller.order.queue";
    public static final String SELLER_ORDER_KEY = "email.seller.order";
    public static final String SELLER_ORDER_MAIL_SUBJECT = "BookHeaven - New Order Received!";

    public static final String ORDER_SHIPPED_QUEUE = "email.order.shipped.queue";
    public static final String ORDER_SHIPPED_KEY = "email.order.shipped";
    public static final String ORDER_SHIPPED_MAIL_SUBJECT = "BookHeaven - Your Order Has Been Shipped! 🚚";

    public static final String ORDER_DELIVERED_QUEUE = "email.order.delivered.queue";
    public static final String ORDER_DELIVERED_KEY = "email.order.delivered";
    public static final String ORDER_DELIVERED_MAIL_SUBJECT = "BookHeaven - Your Order Has Been Delivered! 🎉";

    public static final String CONTACT_QUEUE = "email.contact.queue";
    public static final String CONTACT_KEY = "contact.key";
    public static final String CONTACT_MAIL_SUBJECT = "BookHeaven - New Contact Request! 📨";

    public static final String MISSING_RAZORPAY_QUEUE = "email.missing.razorpay.queue";
    public static final String MISSING_RAZORPAY_KEY = "email.missing.razorpay";
    public static final String MISSING_RAZORPAY_SUBJECT = "Action Required: Update your Razorpay Account to receive your payout! ⚠️";
}

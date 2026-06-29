package com.bookheaven.common.constant;

public class RabbitMqConstant {

    // Timeout Flow
    public static final String ORDER_TIMEOUT_EXCHANGE = "order.timeout.exchange";
    public static final String ORDER_TIMEOUT_QUEUE = "order.timeout.queue";
    public static final String ORDER_TIMEOUT_PROCESSING_QUEUE = "order.timeout.processing.queue";
    public static final String ORDER_TIMEOUT_KEY = "order.timeout";
    public static final String ORDER_TIMEOUT_PROCESSING_KEY = "order.timeout.processing";


    // Email Exchange (already exists in email service)
    public static final String EMAIL_EXCHANGE = "email.exchange";
    public static final String ORDER_CONFIRMED_KEY = "email.order.confirmed";
    public static final String PAYMENT_FAILED_KEY = "email.payment.failed";
    public static final String SELLER_ORDER_KEY = "email.seller.order";
    public static final String ORDER_SHIPPED_KEY = "email.order.shipped";
    public static final String ORDER_DELIVERED_KEY = "email.order.delivered";

    // Ledger Flow
    public static final String LEDGER_EXCHANGE = "ledger.exchange";
    public static final String LEDGER_KEY = "ledger.order.confirmed";

    // Refund Flow
    public static final String REFUND_EXCHANGE = "refund.exchange";
    public static final String REFUND_DELAY_QUEUE = "refund.delay.queue";
    public static final String REFUND_DELAY_KEY = "refund.delay.key";
    public static final String REFUND_PROCESSING_QUEUE = "refund.processing.queue";
    public static final String REFUND_PROCESSING_KEY = "refund.processing.key";
}

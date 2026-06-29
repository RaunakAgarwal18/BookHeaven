package com.bookheaven.payment_service.component;

import com.bookheaven.common.dto.event.RefundEvent;
import com.bookheaven.payment_service.dto.requestDto.RefundRequest;
import com.bookheaven.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.bookheaven.common.constant.RabbitMqConstant.REFUND_PROCESSING_QUEUE;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefundQueueConsumer {

    private final PaymentService paymentService;

    @RabbitListener(queues = REFUND_PROCESSING_QUEUE)
    public void processDelayedRefund(RefundEvent event) {
        log.info("Processing delayed refund for order {}: amount {}", event.getOrderId(), event.getAmount());
        try {
            RefundRequest request = new RefundRequest();
            request.setEventId(event.getEventId());
            request.setOrderId(event.getOrderId());
            request.setAmount(event.getAmount());
            request.setReason(event.getReason());
            
            paymentService.refund(request);
            log.info("Successfully processed delayed refund for order {}", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to process delayed refund for order {}: {}", event.getOrderId(), e.getMessage());
            throw e; // Triggers RabbitMQ retry mechanism
        }
    }
}

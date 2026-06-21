package com.bookheaven.payment_service.component;

import com.bookheaven.payment_service.dto.event.OrderLedgerEvent;
import com.bookheaven.payment_service.entity.SellerLedger;
import com.bookheaven.payment_service.repository.SellerLedgerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class LedgerQueueConsumer {

    private final SellerLedgerRepository ledgerRepository;
    private static final double COMMISSION_RATE = 0.10; // 10% platform cut

    @RabbitListener(queues = "seller-ledger-queue")
    @Transactional
    public void processLedgerEntry(OrderLedgerEvent event) {
        log.info("Received OrderLedgerEvent for order: {}", event.getOrderId());
        try {
            double remainingDiscount = event.getDiscountAmount() != null ? event.getDiscountAmount() : 0.0;

            for (OrderLedgerEvent.LedgerItemDto item : event.getItems()) {
                double gross = item.getPrice() * item.getQuantity();
                
                // Allocate discount if this item belongs to the discountSellerId
                double itemDiscount = 0.0;
                if (event.getDiscountSellerId() != null 
                    && event.getDiscountSellerId().equals(item.getSellerId()) 
                    && remainingDiscount > 0) {
                    
                    // Deduct from remaining discount pool (capped at item's gross)
                    itemDiscount = Math.min(gross, remainingDiscount);
                    remainingDiscount -= itemDiscount;
                }
                
                double commission = gross * COMMISSION_RATE;
                double net = gross - commission - itemDiscount;

                SellerLedger ledger = SellerLedger.builder()
                        .orderId(event.getOrderId())
                        .orderItemId(item.getOrderItemId())
                        .sellerId(item.getSellerId())
                        .grossAmount(gross)
                        .sellerDiscount(itemDiscount)
                        .commissionRate(COMMISSION_RATE)
                        .commissionAmount(commission)
                        .netPayout(net)
                        .currency(item.getCurrency())
                        .settlementStatus(SellerLedger.SettlementStatus.PENDING)
                        .build();

                ledgerRepository.save(ledger);
                log.info("Recorded ledger entry for seller: {}, item ID: {}, net: {}", 
                        item.getSellerId(), item.getOrderItemId(), net);
            }
        } catch (Exception e) {
            log.error("Failed to process ledger entries for order: {}", event.getOrderId(), e);
            throw e; // trigger RabbitMQ retry mechanisms
        }
    }
}

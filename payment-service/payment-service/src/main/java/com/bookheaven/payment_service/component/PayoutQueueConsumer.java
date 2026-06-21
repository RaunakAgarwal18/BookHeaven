package com.bookheaven.payment_service.component;

import com.bookheaven.payment_service.dto.event.SellerPayoutEvent;
import com.bookheaven.payment_service.dto.responseDto.UserResponse;
import com.bookheaven.payment_service.entity.SellerLedger;
import com.bookheaven.payment_service.repository.SellerLedgerRepository;
import com.bookheaven.payment_service.service.UserClient;
import com.razorpay.RazorpayClient;
import com.razorpay.Transfer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.bookheaven.payment_service.dto.event.MissingRazorpayIdEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class PayoutQueueConsumer {

    private final SellerLedgerRepository ledgerRepository;
    private final RazorpayClient razorpayClient;
    private final UserClient userClient;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "seller-payout-queue")
    @Transactional
    public void executePayout(SellerPayoutEvent event) {
        log.info("Received SellerPayoutEvent. Seller: {}, Settlement ID: {}, Net Amount: {}",
                event.getSellerId(), event.getSettlementId(), event.getNetAmount());

        try {
            // 1. Fetch the seller's profile
            UserResponse seller = userClient.getUserById(event.getSellerId());
            if (seller == null || seller.getRazorpayAccountId() == null || seller.getRazorpayAccountId().trim().isEmpty()) {
                String errorMsg = "Seller " + event.getSellerId() + " does not have a linked Razorpay Account ID.";
                log.error(errorMsg);
                ledgerRepository.updateStatusBySettlementId(event.getSettlementId(), SellerLedger.SettlementStatus.FAILED, null);
                
                // Publish missing Razorpay ID email event
                if (seller != null && seller.getEmail() != null) {
                    MissingRazorpayIdEvent missingEvent = MissingRazorpayIdEvent.builder()
                            .email(seller.getEmail())
                            .username(seller.getUsername() != null ? seller.getUsername() : "Seller")
                            .amountPending(event.getNetAmount())
                            .currency(event.getCurrency() != null ? event.getCurrency() : "INR")
                            .build();
                    rabbitTemplate.convertAndSend("email.exchange", "email.missing.razorpay", missingEvent);
                    log.info("Published MissingRazorpayIdEvent to email.exchange for seller: {}", seller.getEmail());
                }
                
                return;
            }

            String razorpayAccountId = seller.getRazorpayAccountId();
            log.info("Executing Razorpay Route transfer for Seller: {}, Linked Account: {}",
                    event.getSellerId(), razorpayAccountId);

            // 2. Build transfer payload (amount in paise, e.g. INR 100.00 = 10000 paise)
            JSONObject transferRequest = new JSONObject();
            transferRequest.put("account", razorpayAccountId);
            transferRequest.put("amount", (int) Math.round(event.getNetAmount() * 100));
            transferRequest.put("currency", event.getCurrency());
            
            JSONObject notes = new JSONObject();
            notes.put("notes", new JSONObject()
                    .put("settlement_id", event.getSettlementId().toString())
                    .put("seller_id", event.getSellerId().toString())
            );
            transferRequest.put("notes", notes);

            // 3. Call Razorpay Transfers API
            Transfer transfer = razorpayClient.transfers.create(transferRequest);
            String gatewayTransferId = transfer.get("id");

            log.info("Successfully executed transfer. Razorpay Transfer ID: {}", gatewayTransferId);

            // 4. Mark Ledger entries as SETTLED
            ledgerRepository.updateStatusBySettlementId(event.getSettlementId(), SellerLedger.SettlementStatus.SETTLED, gatewayTransferId);

        } catch (Exception e) {
            log.error("Failed to execute transfer for settlement: {}", event.getSettlementId(), e);
            // Flag ledger status to FAILED in the database
            try {
                ledgerRepository.updateStatusBySettlementId(event.getSettlementId(), SellerLedger.SettlementStatus.FAILED, null);
            } catch (Exception dbEx) {
                log.error("Failed to update settlement status to FAILED in db", dbEx);
            }
            throw new RuntimeException("Payout failed for settlement: " + event.getSettlementId(), e);
        }
    }
}

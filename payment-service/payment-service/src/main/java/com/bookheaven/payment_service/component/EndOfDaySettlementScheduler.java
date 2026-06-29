package com.bookheaven.payment_service.component;

import com.bookheaven.common.dto.event.SellerPayoutEvent;
import com.bookheaven.payment_service.entity.SellerLedger;
import com.bookheaven.payment_service.repository.SellerLedgerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class EndOfDaySettlementScheduler {

    private final SellerLedgerRepository ledgerRepository;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(cron = "0 50 23 1,15 * ?") // 11:50 PM on the 1st and 15th of every month
    @Transactional
    public void triggerBiWeeklySettlement() {
        log.info("Triggering Bi-Weekly Seller Payout Consolidation Batch Job");
        executeSettlementAggregation();
    }

    @Transactional
    public void executeSettlementAggregation() {
        // 1. Calculate the escrow cutoff date (8 days ago) to respect the 7-day refund window
        java.time.LocalDateTime escrowCutoff = java.time.LocalDateTime.now().minusDays(8);
        
        // 2. Fetch all pending ledger entries that have survived the escrow period
        List<SellerLedger> pendingEntries = ledgerRepository.findEligibleForPayout(SellerLedger.SettlementStatus.PENDING, escrowCutoff);
        if (pendingEntries.isEmpty()) {
            log.info("No PENDING ledger entries found for consolidation that have passed the 8-day escrow period.");
            return;
        }

        log.info("Consolidating {} PENDING ledger entries for payouts...", pendingEntries.size());

        // 2. Group by Seller
        Map<UUID, List<SellerLedger>> groupedBySeller = pendingEntries.stream()
                .collect(Collectors.groupingBy(SellerLedger::getSellerId));

        // 3. Aggregate each seller's net payouts into exactly ONE payout event
        for (Map.Entry<UUID, List<SellerLedger>> entry : groupedBySeller.entrySet()) {
            UUID sellerId = entry.getKey();
            List<SellerLedger> ledgers = entry.getValue();

            double totalNet = ledgers.stream().mapToDouble(SellerLedger::getNetPayout).sum();
            List<Long> ledgerIds = ledgers.stream().map(SellerLedger::getId).toList();
            UUID settlementId = UUID.randomUUID();

            // Set state to PROCESSING and set the batch settlement ID
            ledgers.forEach(ledger -> {
                ledger.setSettlementStatus(SellerLedger.SettlementStatus.PROCESSING);
                ledger.setSettlementId(settlementId);
            });
            ledgerRepository.saveAll(ledgers);

            SellerPayoutEvent payoutEvent = SellerPayoutEvent.builder()
                    .eventId(UUID.randomUUID())
                    .settlementId(settlementId)
                    .sellerId(sellerId)
                    .netAmount(totalNet)
                    .currency(ledgers.getFirst().getCurrency())
                    .ledgerIds(ledgerIds)
                    .settlementDate(LocalDate.now())
                    .build();

            log.info("Enqueuing payout event. Seller: {}, Settlement ID: {}, Amount: {}",
                    sellerId, settlementId, totalNet);

            // Publish to RabbitMQ payout queue after commit
            org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        rabbitTemplate.convertAndSend("seller-payout-exchange", "payout.routing.key", payoutEvent);
                    }
                }
            );
        }
    }

    @Scheduled(cron = "0 0 12 * * ?") // 12:00 PM daily
    @Transactional
    public void retryFailedSettlements() {
        int resetCount = ledgerRepository.resetFailedToPending();
        if (resetCount > 0) {
            log.info("Reset {} FAILED ledger entries back to PENDING for retry.", resetCount);
        }
    }
}

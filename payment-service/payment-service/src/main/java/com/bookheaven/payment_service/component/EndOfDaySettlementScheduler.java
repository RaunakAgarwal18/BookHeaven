package com.bookheaven.payment_service.component;

import com.bookheaven.payment_service.dto.event.SellerPayoutEvent;
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

    @Scheduled(cron = "0 50 23 * * ?") // 11:50 PM daily
    @Transactional
    public void triggerDailySettlement() {
        log.info("Triggering EOD Seller Payout Consolidation Batch Job");
        executeSettlementAggregation();
    }

    @Transactional
    public void executeSettlementAggregation() {
        // 1. Fetch all pending ledger entries
        List<SellerLedger> pendingEntries = ledgerRepository.findBySettlementStatus(SellerLedger.SettlementStatus.PENDING);
        if (pendingEntries.isEmpty()) {
            log.info("No PENDING ledger entries found for consolidation.");
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
                    .settlementId(settlementId)
                    .sellerId(sellerId)
                    .netAmount(totalNet)
                    .currency(ledgers.getFirst().getCurrency())
                    .ledgerIds(ledgerIds)
                    .settlementDate(LocalDate.now())
                    .build();

            log.info("Enqueuing payout event. Seller: {}, Settlement ID: {}, Amount: {}",
                    sellerId, settlementId, totalNet);

            // Publish to RabbitMQ payout queue
            rabbitTemplate.convertAndSend("seller-payout-exchange", "payout.routing.key", payoutEvent);
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

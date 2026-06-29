package com.bookheaven.payment_service.repository;

import com.bookheaven.payment_service.entity.SellerLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SellerLedgerRepository extends JpaRepository<SellerLedger, Long> {

    List<SellerLedger> findBySettlementStatus(SellerLedger.SettlementStatus settlementStatus);

    @Query("SELECT s FROM SellerLedger s WHERE s.settlementStatus = :status AND s.createdAt < :cutoffDate")
    List<SellerLedger> findEligibleForPayout(
            @Param("status") SellerLedger.SettlementStatus status, 
            @Param("cutoffDate") java.time.LocalDateTime cutoffDate
    );

    @Modifying
    @Query("UPDATE SellerLedger s SET s.settlementStatus = :status, s.gatewayTransferId = :transferId WHERE s.settlementId = :settlementId")
    int updateStatusBySettlementId(
            @Param("settlementId") UUID settlementId,
            @Param("status") SellerLedger.SettlementStatus status,
            @Param("transferId") String transferId
    );

    @Modifying
    @Query("UPDATE SellerLedger s SET s.settlementStatus = 'PENDING', s.settlementId = NULL, s.gatewayTransferId = NULL WHERE s.settlementStatus = 'FAILED'")
    int resetFailedToPending();

    @Modifying
    @Query("UPDATE SellerLedger s SET s.settlementStatus = com.bookheaven.payment_service.entity.SellerLedger.SettlementStatus.VOIDED WHERE s.orderId = :orderId AND s.settlementStatus = com.bookheaven.payment_service.entity.SellerLedger.SettlementStatus.PENDING")
    int voidLedgerEntriesByOrderId(@Param("orderId") UUID orderId);

    @Modifying
    @Query("UPDATE SellerLedger s SET s.settlementStatus = com.bookheaven.payment_service.entity.SellerLedger.SettlementStatus.VOIDED WHERE s.orderItemId IN :orderItemIds AND s.settlementStatus = com.bookheaven.payment_service.entity.SellerLedger.SettlementStatus.PENDING")
    int voidLedgerEntriesByOrderItemIds(@Param("orderItemIds") List<Long> orderItemIds);
}

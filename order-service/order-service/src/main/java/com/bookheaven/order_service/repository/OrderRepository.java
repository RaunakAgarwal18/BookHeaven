package com.bookheaven.order_service.repository;

import com.bookheaven.order_service.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    boolean existsByOrderReference(String orderReference);
    Page<Order> findByUserId(UUID userId, Pageable pageable);
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);
    Page<Order> findByStatusAndUserId(Order.OrderStatus status, UUID userId, Pageable pageable);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.items i WHERE i.sellerId = :sellerId")
    Page<Order> findOrdersBySellerId(@Param("sellerId") UUID sellerId, Pageable pageable);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.items i WHERE i.sellerId = :sellerId AND o.status = :status")
    Page<Order> findOrdersBySellerIdAndStatus(@Param("sellerId") UUID sellerId, @Param("status") Order.OrderStatus status, Pageable pageable);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.items i WHERE i.sellerId = :sellerId AND o.status NOT IN :excludedStatuses")
    Page<Order> findOrdersBySellerIdExcludingStatuses(@Param("sellerId") UUID sellerId, @Param("excludedStatuses") List<Order.OrderStatus> excludedStatuses, Pageable pageable);
}

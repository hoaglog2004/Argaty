package com.argaty.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.argaty.entity.OrderStatusHistory;

/**
 * Repository cho OrderStatusHistory Entity
 */
@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {

    List<OrderStatusHistory> findByOrderIdOrderByCreatedAtDesc(Long orderId);

    List<OrderStatusHistory> findByOrderIdOrderByCreatedAtAsc(Long orderId);
}
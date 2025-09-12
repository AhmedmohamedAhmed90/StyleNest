package com.stylenest.OrderService.repository;

import com.stylenest.OrderService.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    /**
     * Find all orders for a given user ID.
     */
    List<Order> findByUserId(Long userId);
}

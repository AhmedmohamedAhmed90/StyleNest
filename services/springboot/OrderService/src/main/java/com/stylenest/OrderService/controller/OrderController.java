package com.stylenest.OrderService.controller;

import com.stylenest.OrderService.dto.OrderItemRequest;
import com.stylenest.OrderService.model.Order;
import com.stylenest.OrderService.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;

import java.util.List;

@RestController
@RequestMapping("api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * Place a new order.
     * @param userId the user's ID forwarded by API Gateway after JWT validation
     * @param items the list of order items
     * @return the created order
     */
    @PostMapping
    public ResponseEntity<Order> placeOrder(
            @Parameter(name = "X-USER-ID", in = ParameterIn.HEADER, required = true, description = "Authenticated user id")
            @RequestHeader("X-USER-ID") Long userId,
            @RequestBody List<OrderItemRequest> items) {

        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        Order order = orderService.createOrder(userId, items);
        return ResponseEntity.ok(order);
    }

    /**
     * Optional: Get all orders for a user
     */
    @GetMapping
    public ResponseEntity<List<Order>> getUserOrders(
            @Parameter(name = "X-USER-ID", in = ParameterIn.HEADER, required = true, description = "Authenticated user id")
            @RequestHeader("X-USER-ID") Long userId) {

        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        List<Order> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }
}

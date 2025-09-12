// package com.stylenest.OrderService.service;

// import com.stylenest.OrderService.dto.OrderItemRequest;
// import com.stylenest.OrderService.event.OrderEventPublisher;
// import com.stylenest.OrderService.event.StockEvent;
// import com.stylenest.OrderService.model.Order;
// import com.stylenest.OrderService.model.OrderItem;
// import com.stylenest.OrderService.model.OrderStatus;
// import com.stylenest.OrderService.repository.OrderRepository;
// import org.springframework.amqp.rabbit.annotation.RabbitListener;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.LocalDateTime;
// import java.util.List;
// import java.util.stream.Collectors;

// @Service
// public class OrderService {

//     @Autowired
//     private OrderRepository orderRepository;

//     @Autowired
//     private OrderEventPublisher eventPublisher;

//     /**
//      * Create order in CREATED status and publish order.created event.
//      */
//     @Transactional
//     public Order createOrder(Long userId, List<OrderItemRequest> items) {
//         Order order = new Order();
//         order.setUserId(userId);
//         order.setStatus(OrderStatus.CREATED);
//         order.setCreatedAt(LocalDateTime.now());

//         // Map DTO → entity
//         List<OrderItem> orderItems = items.stream().map(dto -> {
//             OrderItem entity = new OrderItem();
//             entity.setOrder(order);
//             entity.setProductId(dto.getProductId());
//             entity.setQuantity(dto.getQuantity());
//             entity.setPrice(dto.getPrice());
//             entity.setReserved(false);
//             return entity;
//         }).collect(Collectors.toList());

//         order.setItems(orderItems);

//         Order savedOrder = orderRepository.save(order);

//         // Publish ORDER_CREATED event for ProductService
//         eventPublisher.publishOrderStatusChanged(savedOrder);

//         return savedOrder;
//     }

//     @Transactional
//     public Order updateOrderStatus(Long orderId, OrderStatus status) {
//         Order order = orderRepository.findById(orderId).orElse(null);
//         if (order == null) return null;

//         order.setStatus(status);
//         switch (status) {
//             case CONFIRMED -> order.setConfirmedAt(LocalDateTime.now());
//             case SHIPPED -> order.setShippedAt(LocalDateTime.now());
//             case PAID -> order.setPaidAt(LocalDateTime.now());
//         }

//         Order updatedOrder = orderRepository.save(order);

//         eventPublisher.publishOrderStatusChanged(updatedOrder);

//         return updatedOrder;
//     }

//     @RabbitListener(queues = "stock.reservation.queue")
//     public void handleStockEvent(StockEvent event) {
//         Order order = orderRepository.findById(event.getOrderId()).orElse(null);
//         if (order == null) {
//             System.err.println("Order not found for stock event: " + event);
//             return;
//         }

//         if (event.isInStock()) {
//             order.setStatus(OrderStatus.CONFIRMED);
//             order.setConfirmedAt(LocalDateTime.now());
//         } else {
//             order.setStatus(OrderStatus.CANCELLED);
//         }

//         orderRepository.save(order);
//         eventPublisher.publishOrderStatusChanged(order);
//     }
// }


package com.stylenest.OrderService.service;

import com.stylenest.OrderService.dto.OrderItemRequest;
import com.stylenest.OrderService.event.OrderEventPublisher;
import com.stylenest.OrderService.event.StockEvent;
import com.stylenest.OrderService.model.Order;
import com.stylenest.OrderService.model.OrderItem;
import com.stylenest.OrderService.model.OrderStatus;
import com.stylenest.OrderService.repository.OrderRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderEventPublisher eventPublisher;

    /**
     * Create order in CREATED status and publish order.created event.
     */
    @Transactional
    public Order createOrder(Long userId, List<OrderItemRequest> items) {
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(LocalDateTime.now());

        // Map DTO → entity
        List<OrderItem> orderItems = items.stream().map(dto -> {
            OrderItem entity = new OrderItem();
            entity.setOrder(order);
            entity.setProductId(dto.getProductId());
            entity.setQuantity(dto.getQuantity());
            entity.setPrice(dto.getPrice());
            entity.setReserved(false);
            return entity;
        }).collect(Collectors.toList());

        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        // Publish ORDER_CREATED event for ProductService
        eventPublisher.publishOrderStatusChanged(savedOrder);

        return savedOrder;
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return null;

        order.setStatus(status);
        switch (status) {
            case CONFIRMED -> order.setConfirmedAt(LocalDateTime.now());
            case SHIPPED -> order.setShippedAt(LocalDateTime.now());
            case PAID -> order.setPaidAt(LocalDateTime.now());
        }

        Order updatedOrder = orderRepository.save(order);

        eventPublisher.publishOrderStatusChanged(updatedOrder);

        return updatedOrder;
    }

    @RabbitListener(queues = "stock.reservation.queue")
    public void handleStockEvent(StockEvent event) {
        Order order = orderRepository.findById(event.getOrderId()).orElse(null);
        if (order == null) {
            System.err.println("Order not found for stock event: " + event);
            return;
        }

        if (event.isInStock()) {
            order.setStatus(OrderStatus.CONFIRMED);
            order.setConfirmedAt(LocalDateTime.now());
        } else {
            order.setStatus(OrderStatus.CANCELLED);
        }

        orderRepository.save(order);
        eventPublisher.publishOrderStatusChanged(order);
    }

    /**
     * Get all orders for a user by their ID.
     */
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }
}

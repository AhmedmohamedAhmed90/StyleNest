// // OrderService/event/StockReservationConsumer.java
// package com.stylenest.OrderService.event;

// import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
// import com.stylenest.OrderService.model.Order;
// import com.stylenest.OrderService.model.OrderItem;
// import com.stylenest.OrderService.model.OrderStatus;
// import com.stylenest.OrderService.repository.OrderRepository;
// import com.stylenest.OrderService.service.OrderService;
// import lombok.Data;
// import lombok.NoArgsConstructor;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.amqp.rabbit.annotation.RabbitListener;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.util.Optional;

// @Slf4j
// @Service
// @RequiredArgsConstructor
// public class StockReservationConsumer {

//     private final OrderRepository orderRepository;
//     private final OrderService orderService;

//     @Data
//     @NoArgsConstructor
//     @JsonIgnoreProperties(ignoreUnknown = true)
//     static class StockReservationEventDTO {
//         private String  orderId;     // ProductService sends String; we’ll parse to Long
//         private Long    productId;
//         private Integer quantity;
//         private String  eventType;   // "RESERVE", "RESERVE_FAILED", "RELEASE"
//     }

//     @Transactional
//     @RabbitListener(queues = "stock.reservation.queue")
//     public void onStockReservation(StockReservationEventDTO evt) {
//         log.info("stock.reservation received: {}", evt);

//         Long oid = null;
//         try { oid = Long.valueOf(evt.getOrderId()); } catch (Exception ignore) {}
//         if (oid == null || evt.getProductId() == null) {
//             log.warn("Invalid reservation event: {}", evt);
//             return;
//         }

//         Optional<Order> maybeOrder = orderRepository.findById(oid);
//         if (maybeOrder.isEmpty()) {
//             log.warn("Order {} not found for event {}", oid, evt);
//             return;
//         }

//         Order order = maybeOrder.get();
//         OrderItem item = order.getItems().stream()
//                 .filter(i -> evt.getProductId().equals(i.getProductId()))
//                 .findFirst()
//                 .orElse(null);

//         if (item == null) {
//             log.warn("Order {} has no item with productId {}", oid, evt.getProductId());
//             return;
//         }

//         String type = evt.getEventType() == null ? "" : evt.getEventType().toUpperCase();
//         switch (type) {
//             case "RESERVE" -> item.setReserved(true);
//             case "RESERVE_FAILED", "RELEASE" -> item.setReserved(false);
//             default -> log.warn("Unknown eventType={} for order {}", type, oid);
//         }

//         orderRepository.save(order);
//         log.info("Order {} item productId {} reserved={}", oid, evt.getProductId(), item.isReserved());

//         // Optional: auto-confirm when all items are reserved
//         boolean allReserved = order.getItems().stream().allMatch(OrderItem::isReserved);
//         if (allReserved) {
//             orderService.updateOrderStatus(oid, OrderStatus.CONFIRMED);
//             log.info("Order {} auto-confirmed because all items reserved", oid);
//         }
//     }
// }


package com.stylenest.OrderService.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.stylenest.OrderService.model.Order;
import com.stylenest.OrderService.model.OrderItem;
import com.stylenest.OrderService.model.OrderStatus;
import com.stylenest.OrderService.repository.OrderRepository;
import com.stylenest.OrderService.service.OrderService;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockReservationConsumer {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StockReservationEvent {
        private String orderId;        // ProductService sends String; we parse to Long
        private Long productId;
        private Integer quantity;
        private String userId;
        private LocalDateTime timestamp;
        private String eventType;      // "RESERVE", "RESERVE_FAILED", "RELEASE"
    }

    @Transactional
    @RabbitListener(queues = "stock.reservation.queue")
    public void onStockReservation(StockReservationEvent evt) {
        log.info("stock.reservation received: {}", evt);

        Long oid = null;
        try { oid = Long.valueOf(evt.getOrderId()); } catch (Exception ignore) {}
        if (oid == null || evt.getProductId() == null) {
            log.warn("Invalid reservation event: {}", evt);
            return;
        }

        Optional<Order> maybeOrder = orderRepository.findById(oid);
        if (maybeOrder.isEmpty()) {
            log.warn("Order {} not found for event {}", oid, evt);
            return;
        }

        Order order = maybeOrder.get();
        OrderItem item = order.getItems().stream()
                .filter(i -> evt.getProductId().equals(i.getProductId()))
                .findFirst()
                .orElse(null);

        if (item == null) {
            log.warn("Order {} has no item with productId {}", oid, evt.getProductId());
            return;
        }

        String type = evt.getEventType() == null ? "" : evt.getEventType().toUpperCase();
        switch (type) {
            case "RESERVE" -> item.setReserved(true);
            case "RESERVE_FAILED", "RELEASE" -> item.setReserved(false);
            default -> log.warn("Unknown eventType={} for order {}", type, oid);
        }

        orderRepository.save(order);
        log.info("Order {} item pid {} reserved={}", oid, evt.getProductId(), item.isReserved());

        // Policy: if any item failed → CANCELLED; if all reserved → CONFIRMED
        if ("RESERVE_FAILED".equals(type)) {
            orderService.updateOrderStatus(oid, OrderStatus.CANCELLED);
            log.info("Order {} cancelled due to reservation failure", oid);
            return;
        }

        // Keep order in CREATED until payment; do not auto-confirm on reservation success
        // boolean allReserved = order.getItems().stream().allMatch(OrderItem::isReserved);
        // if (allReserved) {
        //     orderService.updateOrderStatus(oid, OrderStatus.CONFIRMED);
        //     log.info("Order {} auto-confirmed (all items reserved)", oid);
        // }
    }
}

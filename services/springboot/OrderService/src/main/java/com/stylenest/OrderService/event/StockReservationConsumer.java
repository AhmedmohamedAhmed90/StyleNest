package com.stylenest.OrderService.event;

import com.stylenest.OrderService.model.Order;
import com.stylenest.OrderService.model.OrderItem;
import com.stylenest.OrderService.model.OrderStatus;
import com.stylenest.OrderService.repository.OrderRepository;
import com.stylenest.OrderService.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class StockReservationConsumer {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    @RabbitListener(queues = "stock.reservation.queue")
    @Transactional
    public void onStockReservationEvent(StockReservationEvent event) {
        // parse orderId (product published as String)
        Long orderId;
        try {
            orderId = Long.valueOf(event.getOrderId());
        } catch (Exception ex) {
            System.err.println("Invalid orderId in event: " + event.getOrderId());
            return;
        }

        Optional<Order> opt = orderRepository.findById(orderId);
        if (opt.isEmpty()) {
            System.err.println("Order not found for id: " + orderId);
            return;
        }

        Order order = opt.get();

        // Find matching item
        OrderItem matched = null;
        for (OrderItem item : order.getItems()) {
            if (item.getProductId().equals(Long.valueOf(event.getProductId()))) {
                matched = item;
                break;
            }
        }

        if (matched == null) {
            System.err.println("No matching item for productId " + event.getProductId() + " in order " + orderId);
            return;
        }

        String evt = event.getEventType() != null ? event.getEventType().toUpperCase() : "";

        if (evt.contains("RESERVE") && !evt.contains("FAILED")) {
            if (!matched.isReserved()) {
                matched.setReserved(true);
                orderRepository.save(order);
            }
        } else if (evt.contains("RESERVE_FAILED") || evt.contains("INSUFFICIENT")) {
            // reservation failed -> cancel order and optionally release any reserved items
            orderService.updateOrderStatus(orderId, OrderStatus.CANCELLED);
            // Optionally call ProductService to release already reserved items for this order.
        } else if (evt.contains("RELEASE")) {
            if (matched.isReserved()) {
                matched.setReserved(false);
                orderRepository.save(order);
            }
        }

        // if all items reserved -> confirm
        boolean allReserved = order.getItems().stream().allMatch(OrderItem::isReserved);
        if (allReserved) {
            orderService.updateOrderStatus(orderId, OrderStatus.CONFIRMED);
        }
    }
}

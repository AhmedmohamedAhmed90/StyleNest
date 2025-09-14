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
import io.micrometer.core.instrument.MeterRegistry;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentSuccessConsumer {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;
    private final MeterRegistry meterRegistry;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PaymentEvent {
        private String orderId;  // PaymentService sends string ids
        private String userId;
        private Double amount;
        private String status;   // SUCCESS or FAILURE
    }

    @Transactional
    @RabbitListener(queues = "order.payment.success.queue")
    public void onPaymentEvent(PaymentEvent evt) {
        if (evt == null) return;
        String status = evt.getStatus() == null ? "" : evt.getStatus().toUpperCase();
        Long oid = null;
        try { oid = Long.valueOf(evt.getOrderId()); } catch (Exception ignore) {}
        if (oid == null) {
            log.warn("Payment event has invalid orderId: {}", evt.getOrderId());
            return;
        }

        if ("SUCCESS".equals(status)) {
            orderService.updateOrderStatus(oid, OrderStatus.CONFIRMED);
            log.info("Order {} marked CONFIRMED from payment success", oid);
            try { meterRegistry.counter("orders_payment_events", "status", "SUCCESS").increment(); } catch (Exception ignore) {}
        } else if ("FAILURE".equals(status)) {
            // keep CREATED or set CANCELLED per business rule; here we cancel
            orderService.updateOrderStatus(oid, OrderStatus.CANCELLED);
            log.info("Order {} cancelled due to payment failure", oid);
            try { meterRegistry.counter("orders_payment_events", "status", "FAILURE").increment(); } catch (Exception ignore) {}

            // Publish order.cancelled with items so ProductService can release stock
            Order order = orderRepository.findById(oid).orElse(null);
            if (order != null) {
                OrderCreatedEvent evtOut = new OrderCreatedEvent();
                evtOut.setOrderId(order.getId());
                evtOut.setUserId(order.getUserId());
                java.util.List<OrderItemEvent> items = new java.util.ArrayList<>();
                for (OrderItem it : order.getItems()) {
                    OrderItemEvent e = new OrderItemEvent();
                    e.setProductId(it.getProductId());
                    e.setQuantity(it.getQuantity());
                    e.setPrice(it.getPrice());
                    items.add(e);
                }
                evtOut.setItems(items);
                eventPublisher.publishOrderCancelled(evtOut);
            }
        } else {
            log.warn("Unknown payment status '{}' for order {}", status, oid);
            try { meterRegistry.counter("orders_payment_events", "status", "UNKNOWN").increment(); } catch (Exception ignore) {}
        }
    }
}

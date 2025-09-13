// ProductService/messaging/OrderCreatedListener.java
package com.stylenest.ProductService.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.stylenest.ProductService.service.ProductService;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedListener {

    private final ProductService productService;

    @Data @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        private Long productId;
        private Integer quantity;
        private Double price;
    }

    @Data @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OrderCreatedDTO {
        // OrderService may send Long; we’ll accept String (Jackson can coerce),
        // or change to Object and String.valueOf(...) if you prefer.
        private String orderId;
        private String userId;
        private List<Item> items;
    }

    @RabbitListener(queues = "order.created.queue")
    public void onOrderCreated(OrderCreatedDTO msg) {
        if (msg == null || msg.getItems() == null || msg.getItems().isEmpty()) {
            log.warn("order.created received but items are empty: {}", msg);
            return;
        }
        log.info("order.created received: orderId={} items={}", msg.getOrderId(), msg.getItems().size());

        String orderId = String.valueOf(msg.getOrderId()); // normalize to String for your ProductService API
        String userId  = String.valueOf(msg.getUserId());

        for (Item it : msg.getItems()) {
            if (it == null || it.getProductId() == null || it.getQuantity() == null) continue;

            boolean ok = productService.reserveStock(
                    it.getProductId().intValue(),  // your reserveStock(Integer,..)
                    it.getQuantity(),
                    orderId,
                    userId
            );

            log.info("reserveStock result: orderId={} productId={} qty={} ok={}",
                    orderId, it.getProductId(), it.getQuantity(), ok);
        }
    }
}

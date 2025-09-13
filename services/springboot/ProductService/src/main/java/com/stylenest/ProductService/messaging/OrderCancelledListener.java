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
public class OrderCancelledListener {

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
    public static class OrderCancelledDTO {
        private String orderId;
        private String userId;
        private List<Item> items;
    }

    @RabbitListener(queues = "order.cancelled.queue")
    public void onOrderCancelled(OrderCancelledDTO msg) {
        if (msg == null || msg.getItems() == null || msg.getItems().isEmpty()) {
            log.warn("order.cancelled received but items are empty: {}", msg);
            return;
        }
        String orderId = String.valueOf(msg.getOrderId());
        String userId = String.valueOf(msg.getUserId());
        log.info("order.cancelled received: orderId={} items={}", orderId, msg.getItems().size());

        for (Item it : msg.getItems()) {
            if (it == null || it.getProductId() == null || it.getQuantity() == null) continue;
            boolean ok = productService.releaseStock(
                    it.getProductId().intValue(),
                    it.getQuantity(),
                    orderId,
                    userId
            );
            log.info("releaseStock result: orderId={} productId={} qty={} ok={}",
                    orderId, it.getProductId(), it.getQuantity(), ok);
        }
    }
}


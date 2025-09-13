// src/main/java/com/stylenest/ProductService/event/OrderCreatedEvent.java
package com.stylenest.ProductService.event;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {
    private String orderId;
    private String userId;            // optional; keep if you include it upstream
    private List<OrderItemEvent> items;
}

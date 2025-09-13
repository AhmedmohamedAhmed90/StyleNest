// src/main/java/com/stylenest/ProductService/event/OrderItemEvent.java
package com.stylenest.ProductService.event;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemEvent {
    private Integer productId;
    private Integer quantity;
}

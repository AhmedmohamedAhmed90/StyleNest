// package com.stylenest.OrderService.event;

// public class OrderItemEvent {
//     private Long productId;
//     private Integer quantity;

//     public OrderItemEvent() {}

//     public Long getProductId() { return productId; }
//     public void setProductId(Long productId) { this.productId = productId; }
//     public Integer getQuantity() { return quantity; }
//     public void setQuantity(Integer quantity) { this.quantity = quantity; }
// }

package com.stylenest.OrderService.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderItemEvent {
    private Long productId;
    private Integer quantity;
    private Double price;
}


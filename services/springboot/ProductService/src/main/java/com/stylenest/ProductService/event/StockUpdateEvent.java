package com.stylenest.ProductService.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockUpdateEvent {
    private Integer productId;
    private Integer newStock;
    private Integer reservedStock;
    private String orderId;
    private LocalDateTime timestamp;
    private String eventType; // "STOCK_UPDATED", "STOCK_RESERVED", "STOCK_RELEASED"
}

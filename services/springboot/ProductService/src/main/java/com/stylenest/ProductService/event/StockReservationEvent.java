package com.stylenest.ProductService.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockReservationEvent {
    private Integer productId;
    private Integer quantity;
    private String orderId;
    private String userId;
    private LocalDateTime timestamp;
    private String eventType; // "RESERVE", "RELEASE", "RESERVE_FAILED"
}

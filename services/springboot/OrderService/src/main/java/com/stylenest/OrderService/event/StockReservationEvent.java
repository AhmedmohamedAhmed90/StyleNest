package com.stylenest.OrderService.event;

import java.time.LocalDateTime;

public class StockReservationEvent {
    private Integer productId;
    private Integer quantity;
    private String orderId;    // ProductService used String orderId earlier
    private String userId;
    private LocalDateTime timestamp;
    private String eventType; // e.g. "RESERVE", "RESERVE_FAILED", "RELEASE", "RESERVE_SUCCESS"

    public StockReservationEvent() {}

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    @Override
    public String toString() {
        return "StockReservationEvent{" +
                "productId=" + productId +
                ", quantity=" + quantity +
                ", orderId='" + orderId + '\'' +
                ", userId='" + userId + '\'' +
                ", eventType='" + eventType + '\'' +
                '}';
    }
}

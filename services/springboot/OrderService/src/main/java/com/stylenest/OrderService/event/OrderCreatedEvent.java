package com.stylenest.OrderService.event;

import java.util.List;

public class OrderCreatedEvent {
    private Long orderId;
    private Long userId;
    private List<OrderItemEvent> items;

    public OrderCreatedEvent() {}

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public List<OrderItemEvent> getItems() { return items; }
    public void setItems(List<OrderItemEvent> items) { this.items = items; }
}

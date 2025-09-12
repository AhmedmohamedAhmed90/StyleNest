package com.stylenest.OrderService.event;

public class StockEvent {
    private Long orderId;

    private Long productId;

    private int quantity;

    private boolean inStock;

    // Getters and setters

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isInStock() {
        return inStock;
    }

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }

    @Override
    public String toString() {
        return "StockEvent{" + "orderId=" + orderId + ", productId=" + productId + ", quantity=" + quantity + ", inStock=" + inStock + '}';
    }
}

package com.stylenest.OrderService.event;

import com.stylenest.OrderService.model.Order;
import com.stylenest.OrderService.model.OrderStatus;
import com.stylenest.OrderService.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StockEventConsumer {
    @Autowired
    private OrderService orderService;

    @RabbitListener(queues = "stock.adjust.queue")
    public void handleStockEvent(StockEvent event) {
        if (event.isInStock()) {
            orderService.updateOrderStatus(event.getOrderId(), OrderStatus.CONFIRMED);
        } else {
            // Optionally set to a failed/cancelled status or notify
            System.err.println("Stock not available for orderId: " + event.getOrderId());
        }
    }
}

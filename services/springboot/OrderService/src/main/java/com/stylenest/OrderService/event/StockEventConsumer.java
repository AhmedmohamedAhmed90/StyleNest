package com.stylenest.OrderService.event;

import com.stylenest.OrderService.model.OrderStatus;
import com.stylenest.OrderService.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "order.rabbit.stockadjust.enabled", havingValue = "true")
public class StockEventConsumer {
    @Autowired
    private OrderService orderService;

    @RabbitListener(queues = "stock.adjust.queue")
    public void handleStockEvent(StockEvent event) {
        if (event.isInStock()) {
            orderService.updateOrderStatus(event.getOrderId(), OrderStatus.CONFIRMED);
        } else {
            System.err.println("Stock not available for orderId: " + event.getOrderId());
        }
    }
}

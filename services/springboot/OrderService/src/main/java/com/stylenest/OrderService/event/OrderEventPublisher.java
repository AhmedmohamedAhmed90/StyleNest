package com.stylenest.OrderService.event;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderEventPublisher {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishOrderStatusChanged(Object order) {
        rabbitTemplate.convertAndSend("order.status.exchange", "order.status.routingKey", order);
    }

    public void publishOrderCreated(OrderCreatedEvent event) {
        rabbitTemplate.convertAndSend("order.exchange", "order.created", event);
    }
}

// package com.stylenest.OrderService.event;

// import org.springframework.amqp.rabbit.core.RabbitTemplate;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// @Service
// public class OrderEventPublisher {
//     @Autowired
//     private RabbitTemplate rabbitTemplate;

//     public void publishOrderStatusChanged(Object order) {
//         rabbitTemplate.convertAndSend("order.status.exchange", "order.status.routingKey", order);
//     }

//     public void publishOrderCreated(OrderCreatedEvent event) {
//         rabbitTemplate.convertAndSend("order.exchange", "order.created", event);
//     }
// }

package com.stylenest.OrderService.event;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // ✅ Publish to order.exchange / order.created
    public void publishOrderCreated(OrderCreatedEvent event) {
        rabbitTemplate.convertAndSend("order.exchange", "order.created", event);
    }

    public void publishOrderCancelled(OrderCreatedEvent event) {
        rabbitTemplate.convertAndSend("order.exchange", "order.cancelled", event);
    }

    // Optional – only if you have an exchange/queue bound for status.
    // Keep but do NOT call during creation.
    public void publishOrderStatusChanged(Object statusEvent) {
        // If you don't actually consume order status anywhere, you can remove this method
        // or point it to an existing exchange/routing key that you declared.
        // rabbitTemplate.convertAndSend("order.status.exchange", "order.status", statusEvent);
    }
}


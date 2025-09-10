package com.stylenest.ProductService.service;

import com.stylenest.ProductService.config.RabbitMQConfig;
import com.stylenest.ProductService.event.StockReservationEvent;
import com.stylenest.ProductService.event.StockUpdateEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EventPublisherService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishStockReservationEvent(Integer productId, Integer quantity, String orderId, String userId, String eventType) {
        StockReservationEvent event = new StockReservationEvent(
                productId, quantity, orderId, userId, LocalDateTime.now(), eventType
        );
        
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.STOCK_EXCHANGE,
                RabbitMQConfig.STOCK_RESERVATION_ROUTING_KEY,
                event
        );
    }

    public void publishStockUpdateEvent(Integer productId, Integer newStock, Integer reservedStock, String orderId, String eventType) {
        StockUpdateEvent event = new StockUpdateEvent(
                productId, newStock, reservedStock, orderId, LocalDateTime.now(), eventType
        );
        
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.STOCK_EXCHANGE,
                RabbitMQConfig.STOCK_UPDATE_ROUTING_KEY,
                event
        );
    }
}

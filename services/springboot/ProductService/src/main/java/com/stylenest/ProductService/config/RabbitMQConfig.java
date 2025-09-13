package com.stylenest.ProductService.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchange names
    public static final String STOCK_EXCHANGE = "stock.exchange";
    public static final String ORDER_EXCHANGE = "order.exchange";

    // Queue names
    public static final String STOCK_RESERVATION_QUEUE = "stock.reservation.queue";
    public static final String STOCK_UPDATE_QUEUE = "stock.update.queue";
    public static final String ORDER_CREATED_QUEUE = "order.created.queue";
    public static final String ORDER_CANCELLED_QUEUE = "order.cancelled.queue";

    // Routing keys
    public static final String STOCK_RESERVATION_ROUTING_KEY = "stock.reservation";
    public static final String STOCK_UPDATE_ROUTING_KEY = "stock.update";
    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";
    public static final String ORDER_CANCELLED_ROUTING_KEY = "order.cancelled";

// ProductService/config/RabbitMQConfig.java
@Bean
public Jackson2JsonMessageConverter jsonMessageConverter() {
    var conv = new Jackson2JsonMessageConverter();
    // Important: ignore __TypeId__ header and convert based on your method parameter type
    conv.setAlwaysConvertToInferredType(true);
    return conv;
}

@Bean
public RabbitTemplate rabbitTemplate(ConnectionFactory cf, MessageConverter conv) {
    var rt = new RabbitTemplate(cf);
    rt.setMessageConverter(conv);
    return rt;
}

@Bean
public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
        ConnectionFactory cf, MessageConverter conv) {
    var f = new SimpleRabbitListenerContainerFactory();
    f.setConnectionFactory(cf);
    f.setMessageConverter(conv);
    return f;
}

    // Stock Exchange
    @Bean
    public TopicExchange stockExchange() {
        return new TopicExchange(STOCK_EXCHANGE);
    }

    // Order Exchange
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    // Stock Reservation Queue
    @Bean
    public Queue stockReservationQueue() {
        return QueueBuilder.durable(STOCK_RESERVATION_QUEUE).build();
    }

    // Stock Update Queue
    @Bean
    public Queue stockUpdateQueue() {
        return QueueBuilder.durable(STOCK_UPDATE_QUEUE).build();
    }

    // Order Created Queue
    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable(ORDER_CREATED_QUEUE).build();
    }

    // Order Cancelled Queue
    @Bean
    public Queue orderCancelledQueue() {
        return QueueBuilder.durable(ORDER_CANCELLED_QUEUE).build();
    }

    // Bindings
    @Bean
    public Binding stockReservationBinding() {
        return BindingBuilder
                .bind(stockReservationQueue())
                .to(stockExchange())
                .with(STOCK_RESERVATION_ROUTING_KEY);
    }

    @Bean
    public Binding stockUpdateBinding() {
        return BindingBuilder
                .bind(stockUpdateQueue())
                .to(stockExchange())
                .with(STOCK_UPDATE_ROUTING_KEY);
    }

    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder
                .bind(orderCreatedQueue())
                .to(orderExchange())
                .with(ORDER_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding orderCancelledBinding() {
        return BindingBuilder
                .bind(orderCancelledQueue())
                .to(orderExchange())
                .with(ORDER_CANCELLED_ROUTING_KEY);
    }
}

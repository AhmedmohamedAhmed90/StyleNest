// package com.stylenest.OrderService.config;

// import org.springframework.amqp.core.*;
// import org.springframework.amqp.rabbit.core.RabbitTemplate;
// import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;

// @Configuration
// public class RabbitMQConfig {
//     // Exchanges
//     public static final String ORDER_EXCHANGE = "order.exchange";
//     public static final String STOCK_EXCHANGE = "stock.exchange";

//     // Order -> Product routing
//     public static final String ORDER_CREATED_ROUTING_KEY = "order.created";
//     public static final String ORDER_CREATED_QUEUE = "order.created.queue";

//     // Product -> Order (stock reservation events)
//     public static final String STOCK_RESERVATION_ROUTING_KEY = "stock.reservation";
//     public static final String STOCK_RESERVATION_QUEUE = "stock.reservation.queue";

//     // Product -> Order (stock adjust events)
//     public static final String STOCK_ADJUST_ROUTING_KEY = "stock.adjust";
//     public static final String STOCK_ADJUST_QUEUE = "stock.adjust.queue";

//     // Exchanges
//     @Bean
//     public TopicExchange orderExchange() {
//         return new TopicExchange(ORDER_EXCHANGE);
//     }

//     @Bean
//     public TopicExchange stockExchange() {
//         return new TopicExchange(STOCK_EXCHANGE);
//     }

//     // Queues
//     @Bean
//     public Queue orderCreatedQueue() {
//         return new Queue(ORDER_CREATED_QUEUE, true);
//     }

//     @Bean
//     public Queue stockReservationQueue() {
//         return new Queue(STOCK_RESERVATION_QUEUE, true);
//     }

//     @Bean
//     public Queue stockAdjustQueue() {
//         return new Queue(STOCK_ADJUST_QUEUE, true);
//     }

//     // Bindings
//     @Bean
//     public Binding bindOrderCreated(Queue orderCreatedQueue, TopicExchange orderExchange) {
//         return BindingBuilder.bind(orderCreatedQueue).to(orderExchange).with(ORDER_CREATED_ROUTING_KEY);
//     }

//     @Bean
//     public Binding bindStockReservation(Queue stockReservationQueue, TopicExchange stockExchange) {
//         return BindingBuilder.bind(stockReservationQueue).to(stockExchange).with(STOCK_RESERVATION_ROUTING_KEY);
//     }

//     @Bean
//     public Binding bindStockAdjust(Queue stockAdjustQueue, TopicExchange stockExchange) {
//         return BindingBuilder.bind(stockAdjustQueue).to(stockExchange).with(STOCK_ADJUST_ROUTING_KEY);
//     }

//      // JSON message converter
//     @Bean
//     public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
//         return new Jackson2JsonMessageConverter();
//     }

//     // RabbitTemplate configured with JSON converter
//     @Bean
//     public RabbitTemplate rabbitTemplate(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory) {
//         RabbitTemplate template = new RabbitTemplate(connectionFactory);
//         template.setMessageConverter(jackson2JsonMessageConverter());
//         return template;
//     }
// }


package com.stylenest.OrderService.config;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Use inferred type so we ignore __TypeId__ from other services
    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter conv = new Jackson2JsonMessageConverter();
        conv.setAlwaysConvertToInferredType(true);
        return conv;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory cf, MessageConverter conv) {
        SimpleRabbitListenerContainerFactory f = new SimpleRabbitListenerContainerFactory();
        f.setConnectionFactory(cf);
        f.setMessageConverter(conv);
        return f;
    }
}

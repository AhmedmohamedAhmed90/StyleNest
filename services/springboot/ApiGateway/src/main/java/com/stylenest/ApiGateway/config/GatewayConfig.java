package com.stylenest.ApiGateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("product-service", r -> r.path("/api/products/**")
                        .filters(f -> f.circuitBreaker(c -> c.setName("productCB")))
                        .uri("lb://PRODUCTSERVICE"))
                .route("category-service", r -> r.path("/api/categories/**")
                        .filters(f -> f.circuitBreaker(c -> c.setName("categoryCB")))
                        .uri("lb://PRODUCTSERVICE"))
                .route("order-service", r -> r.path("/api/orders/**")
                        .filters(f -> f.circuitBreaker(c -> c.setName("orderCB")))
                        .uri("lb://ORDERSERVICE"))
                .route("auth-service", r -> r.path("/auth/**")
                        .filters(f -> f.circuitBreaker(c -> c.setName("authCB")))
                        .uri("lb://AUTHSERVICE"))
                // Fallback routes for services defined via Config Server. These ensure
                // the gateway is functional even if Config Server is late at startup.
                .route("cart-service", r -> r.path("/api/cart/**")
                        .filters(f -> f.circuitBreaker(c -> c.setName("cartCB")))
                        .uri("lb://CARTSERVICE"))
                .route("payment-service", r -> r.path("/api/payments/**")
                        .filters(f -> f.circuitBreaker(c -> c.setName("paymentCB")))
                        .uri("lb://PAYMENTSERVICE"))
                .build();
    }
}

package com.stylenest.ProductService.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MonitoringConfig {

    @Bean
    public Counter productViewCounter(MeterRegistry meterRegistry) {
        return Counter.builder("product.view.total")
                .description("Total number of product views")
                .register(meterRegistry);
    }

    @Bean
    public Counter productSearchCounter(MeterRegistry meterRegistry) {
        return Counter.builder("product.search.total")
                .description("Total number of product searches")
                .register(meterRegistry);
    }

    @Bean
    public Counter productCreatedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("product.created.total")
                .description("Total number of products created")
                .register(meterRegistry);
    }

    @Bean
    public Counter productUpdatedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("product.updated.total")
                .description("Total number of products updated")
                .register(meterRegistry);
    }

    @Bean
    public Timer stockReservationTimer(MeterRegistry meterRegistry) {
        return Timer.builder("product.stock.reservation.duration")
                .description("Time taken for stock reservation operations")
                .register(meterRegistry);
    }
}

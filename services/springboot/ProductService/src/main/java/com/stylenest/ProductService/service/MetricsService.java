package com.stylenest.ProductService.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MetricsService {

    @Autowired
    private Counter productViewCounter;

    @Autowired
    private Counter productSearchCounter;

    @Autowired
    private Counter productCreatedCounter;

    @Autowired
    private Counter productUpdatedCounter;

    @Autowired
    private Timer stockReservationTimer;

    public void incrementProductView() {
        productViewCounter.increment();
    }

    public void incrementProductSearch() {
        productSearchCounter.increment();
    }

    public void incrementProductCreated() {
        productCreatedCounter.increment();
    }

    public void incrementProductUpdated() {
        productUpdatedCounter.increment();
    }

    public Timer.Sample startStockReservationTimer() {
        return Timer.start();
    }

    public void recordStockReservationDuration(Timer.Sample sample) {
        sample.stop(stockReservationTimer);
    }
}

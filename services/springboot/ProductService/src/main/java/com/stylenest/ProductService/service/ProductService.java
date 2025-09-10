package com.stylenest.ProductService.service;

import com.stylenest.ProductService.model.Product;
import com.stylenest.ProductService.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private MetricsService metricsService;
    
    @Autowired
    private EventPublisherService eventPublisherService;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product saveProduct(Product product) {
        boolean isUpdate = product.getProductId() != null;
        
        Product savedProduct = productRepository.save(product);
        
        // Update metrics
        if (isUpdate) {
            metricsService.incrementProductUpdated();
        } else {
            metricsService.incrementProductCreated();
        }
        
        // Publish stock update event
        eventPublisherService.publishStockUpdateEvent(
                savedProduct.getProductId(),
                savedProduct.getStock(),
                0, // No reserved stock for new/updated products
                null,
                "STOCK_UPDATED"
        );
        
        return savedProduct;
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    @Transactional
    public boolean reserveStock(Integer productId, Integer quantity, String orderId, String userId) {
        var timer = metricsService.startStockReservationTimer();
        
        try {
            Optional<Product> productOpt = productRepository.findById(productId.longValue());
            
            if (productOpt.isEmpty()) {
                // Publish failed reservation event
                eventPublisherService.publishStockReservationEvent(
                        productId, quantity, orderId, userId, "RESERVE_FAILED"
                );
                return false;
            }
            
            Product product = productOpt.get();
            
            if (product.getStock() < quantity) {
                // Publish failed reservation event
                eventPublisherService.publishStockReservationEvent(
                        productId, quantity, orderId, userId, "RESERVE_FAILED"
                );
                return false;
            }
            
            // Reserve stock
            product.setStock(product.getStock() - quantity);
            productRepository.save(product);
            
            // Publish successful reservation event
            eventPublisherService.publishStockReservationEvent(
                    productId, quantity, orderId, userId, "RESERVE"
            );
            
            // Publish stock update event
            eventPublisherService.publishStockUpdateEvent(
                    productId, product.getStock(), quantity, orderId, "STOCK_RESERVED"
            );
            
            return true;
        } finally {
            metricsService.recordStockReservationDuration(timer);
        }
    }

    @Transactional
    public boolean releaseStock(Integer productId, Integer quantity, String orderId, String userId) {
        Optional<Product> productOpt = productRepository.findById(productId.longValue());
        
        if (productOpt.isEmpty()) {
            return false;
        }
        
        Product product = productOpt.get();
        
        // Release stock
        product.setStock(product.getStock() + quantity);
        productRepository.save(product);
        
        // Publish stock release event
        eventPublisherService.publishStockReservationEvent(
                productId, quantity, orderId, userId, "RELEASE"
        );
        
        // Publish stock update event
        eventPublisherService.publishStockUpdateEvent(
                productId, product.getStock(), 0, orderId, "STOCK_RELEASED"
        );
        
        return true;
    }

    public List<Product> getProductsByCategory(Integer categoryId) {
        return productRepository.findByCategoryCategoryId(categoryId);
    }
}

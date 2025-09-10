package com.stylenest.ProductService.controller;

import com.stylenest.ProductService.model.Product;
import com.stylenest.ProductService.service.ProductService;
import com.stylenest.ProductService.service.MetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    @Autowired
    private ProductService productService;
    
    @Autowired
    private MetricsService metricsService;

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        metricsService.incrementProductView();
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{categoryId}")
    public List<Product> getProductsByCategory(@PathVariable Integer categoryId) {
        return productService.getProductsByCategory(categoryId);
    }

    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        return productService.saveProduct(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        return productService.getProductById(id)
                .map(existingProduct -> {
                    product.setProductId(existingProduct.getProductId());
                    return ResponseEntity.ok(productService.saveProduct(product));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/reserve")
    public ResponseEntity<String> reserveStock(
            @PathVariable Integer id,
            @RequestParam Integer quantity,
            @RequestParam String orderId,
            @RequestParam String userId) {
        
        boolean success = productService.reserveStock(id, quantity, orderId, userId);
        
        if (success) {
            return ResponseEntity.ok("Stock reserved successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to reserve stock - insufficient quantity or product not found");
        }
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<String> releaseStock(
            @PathVariable Integer id,
            @RequestParam Integer quantity,
            @RequestParam String orderId,
            @RequestParam String userId) {
        
        boolean success = productService.releaseStock(id, quantity, orderId, userId);
        
        if (success) {
            return ResponseEntity.ok("Stock released successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to release stock - product not found");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}

package com.stylenest.ProductService.controller;

import com.stylenest.ProductService.dto.ProductRequest;
import com.stylenest.ProductService.model.Product;
import com.stylenest.ProductService.service.ProductService;
import com.stylenest.ProductService.service.MetricsService;
import com.stylenest.ProductService.service.CategoryService;
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
    
    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Integer id) {
        metricsService.incrementProductView();
        return productService.getProductById(id.longValue())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{categoryId}")
    public List<Product> getProductsByCategory(@PathVariable Integer categoryId) {
        return productService.getProductsByCategory(categoryId);
    }

    @PostMapping
    public Product createProduct(@RequestBody ProductRequest productRequest) {
        Product product = new Product();
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setStock(productRequest.getStock());
        
        // Set category if categoryId is provided
        if (productRequest.getCategoryId() != null) {
            categoryService.getCategoryById(productRequest.getCategoryId())
                    .ifPresent(product::setCategory);
        }
        
        return productService.saveProduct(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Integer id, @RequestBody ProductRequest productRequest) {
        return productService.getProductById(id.longValue())
                .map(existingProduct -> {
                    existingProduct.setName(productRequest.getName());
                    existingProduct.setDescription(productRequest.getDescription());
                    existingProduct.setPrice(productRequest.getPrice());
                    existingProduct.setStock(productRequest.getStock());
                    
                    // Update category if categoryId is provided
                    if (productRequest.getCategoryId() != null) {
                        categoryService.getCategoryById(productRequest.getCategoryId())
                                .ifPresent(existingProduct::setCategory);
                    }
                    
                    return ResponseEntity.ok(productService.saveProduct(existingProduct));
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
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) {
        productService.deleteProduct(id.longValue());
        return ResponseEntity.noContent().build();
    }
}

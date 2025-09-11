package com.stylenest.ProductService.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductRequest {
    private String name;
    private String description;
    private Double price;
    private Integer stock;
    private Integer categoryId; // Only categoryId, not the full category object
}

package com.stylenest.ProductService.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("StyleNest Product Service API")
                        .version("1.0.0")
                        .description("Product Service for StyleNest E-commerce Platform")
                        .contact(new Contact()
                                .name("StyleNest Team")
                                .email("support@stylenest.com")
                                .url("https://stylenest.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}

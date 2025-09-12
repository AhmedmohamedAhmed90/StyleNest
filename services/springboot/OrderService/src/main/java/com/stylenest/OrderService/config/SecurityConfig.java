package com.stylenest.OrderService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()  // allow all for testing
            )
            .formLogin(form -> form.disable()) // disable default login form
            .httpBasic(httpBasic -> httpBasic.disable()); // disable http basic auth

        return http.build();
    }
}


// @Configuration
// public class SecurityConfig {

//     @Bean
//     public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//         http
//             // disable csrf for APIs
//             .csrf(csrf -> csrf.disable())
//             // allow Swagger UI and API docs without auth
//             .authorizeHttpRequests(auth -> auth
//                 .requestMatchers(
//                     "/v3/api-docs/**",
//                     "/swagger-ui.html",
//                     "/swagger-ui/**"
//                 ).permitAll()
//                 // all other endpoints require authentication (JWT check is in controller)
//                 .anyRequest().permitAll()
//             )
//             // disable default login form
//             .httpBasic(httpBasic -> httpBasic.disable())
//             .formLogin(form -> form.disable());

//         return http.build();
//     }
// }




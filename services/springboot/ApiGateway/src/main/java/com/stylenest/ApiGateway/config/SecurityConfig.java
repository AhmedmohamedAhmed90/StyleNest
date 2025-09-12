package com.stylenest.ApiGateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Configuration
public class SecurityConfig {

@Bean
public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    http
        .csrf(csrf -> csrf.disable())
        .httpBasic(httpBasic -> httpBasic.disable())   // disable default basic auth
        .formLogin(formLogin -> formLogin.disable())   // disable default login page
        .authorizeExchange(exchanges -> exchanges
            .pathMatchers("/auth/**").permitAll()      // allow login/register
            .pathMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
            .anyExchange().authenticated()             // everything else requires JWT
        )
        .securityContextRepository(NoOpServerSecurityContextRepository.getInstance()); // no default auth for /auth/**

    return http.build();
}


}

package com.stylenest.ApiGateway.security;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Order(-101) // run before Spring Security (-100) so auth is available
public class JwtAuthFilter implements WebFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Allow OPTIONS (CORS preflight)
        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        // Skip public endpoints (no JWT required)
        if (path.startsWith("/auth")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/actuator")
                || path.startsWith("/api/products")
                || path.startsWith("/api/categories")) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Extract claims
        String email = jwtUtil.getEmailFromToken(token);
        String userId = jwtUtil.getUserIdFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);

        // Forward user context headers downstream
        ServerWebExchange mutated = exchange.mutate()
                .request(exchange.getRequest()
                        .mutate()
                        .header("X-USER-EMAIL", email)
                        .header("X-USER-ID", userId)
                        .build())
                .build();

        // Build Authentication so Security can treat as authenticated
        List<SimpleGrantedAuthority> authorities = (role == null || role.isBlank())
                ? List.of()
                : List.of(new SimpleGrantedAuthority("ROLE_" + role));
        Authentication auth = new UsernamePasswordAuthenticationToken(email, null, authorities);

        return chain
                .filter(mutated)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
    }
}

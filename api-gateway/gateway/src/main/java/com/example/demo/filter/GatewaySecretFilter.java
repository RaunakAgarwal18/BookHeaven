package com.example.demo.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class GatewaySecretFilter implements GlobalFilter, Ordered {

    @Value("${gateway.communication.secret}")
    private String gatewaySecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Mutate request to add header


        ServerHttpRequest request = exchange.getRequest().mutate()
                .header("X-Gateway-Secret", gatewaySecret)
                .build();

        // Pass modified exchange down the chain
        return chain.filter(exchange.mutate().request(request).build());
    }

    @Override
    public int getOrder() {
        // Run early in the gateway filter chain (before routing out)
        return -1;
    }
}

package com.example.demo.config;

import com.example.demo.component.SlidingWindowRateLimiter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

        private final SlidingWindowRateLimiter loginRateLimiter;
        private final SlidingWindowRateLimiter authRateLimiter;
        private final SlidingWindowRateLimiter checkoutRateLimiter;
        private final SlidingWindowRateLimiter standardRateLimiter;

        private final KeyResolver ipKeyResolver;
        private final KeyResolver userKeyResolver;

        @Value("${gateway.communication.secret}")
        private String gatewaySecret;


        public GatewayConfig(
                @Qualifier("loginRateLimiter") SlidingWindowRateLimiter loginRateLimiter,
                @Qualifier("authRateLimiter") SlidingWindowRateLimiter authRateLimiter,
                @Qualifier("checkoutRateLimiter") SlidingWindowRateLimiter checkoutRateLimiter,
                @Qualifier("standardRateLimiter") SlidingWindowRateLimiter standardRateLimiter,
                @Qualifier("ipKeyResolver") KeyResolver ipKeyResolver,
                @Qualifier("userKeyResolver") KeyResolver userKeyResolver
        ) {
                this.loginRateLimiter = loginRateLimiter;
                this.authRateLimiter = authRateLimiter;
                this.checkoutRateLimiter = checkoutRateLimiter;
                this.standardRateLimiter = standardRateLimiter;
                this.ipKeyResolver = ipKeyResolver;
                this.userKeyResolver = userKeyResolver;
        }

        @Bean
        public RouteLocator routes(RouteLocatorBuilder builder) {
            return builder.routes()
                // Book Catalog (General limit)
                .route("book-service", r -> r
                                .path("/api/book", "/api/book/**")
                                .filters(f -> f.requestRateLimiter(c -> c
                                        .setRateLimiter(standardRateLimiter)
                                        .setKeyResolver(ipKeyResolver)
                                ))
                                .uri("lb://BOOK-SERVICE"))

                // Reviews & Ratings (General limit)
                .route("review-service", r -> r
                                .path("/api/reviews/**")
                                .filters(f -> f.requestRateLimiter(c -> c
                                        .setRateLimiter(standardRateLimiter)
                                        .setKeyResolver(ipKeyResolver)
                                ))
                                .uri("lb://REVIEW-SERVICE"))

                // SEARCH SERVICE
                .route("search-service", r -> r
                                .path("/api/search", "/api/search/**")
                                .filters(f -> f.requestRateLimiter(c -> c
                                        .setRateLimiter(standardRateLimiter)
                                        .setKeyResolver(ipKeyResolver)
                                ))
                                .uri("lb://SEARCH-SERVICE"))

                // USER SERVICE - Dedicated High-Security Routes
                .route("user-login", r -> r
                                .path("/api/user/login", "/api/user/login/**")
                                .filters(f -> f.requestRateLimiter(c -> c
                                        .setRateLimiter(loginRateLimiter)
                                        .setKeyResolver(ipKeyResolver)
                                ))
                                .uri("lb://USER-SERVICE"))

                .route("user-auth", r -> r
                                .path("/api/user/auth/**")
                                .filters(f -> f.requestRateLimiter(c -> c
                                        .setRateLimiter(authRateLimiter)
                                        .setKeyResolver(ipKeyResolver)
                                ))
                                .uri("lb://USER-SERVICE"))

                // Remaining general user profile requests
                .route("user-service", r -> r
                                .path("/api/user/**")
                                .filters(f -> f.requestRateLimiter(c -> c
                                        .setRateLimiter(standardRateLimiter)
                                        .setKeyResolver(userKeyResolver)
                                ))
                                .uri("lb://USER-SERVICE"))

                // Shopping Cart
                .route("cart-service", r -> r
                                .path("/api/cart/**", "/api/coupons/**")
                                .filters(f -> f.requestRateLimiter(c -> c
                                        .setRateLimiter(standardRateLimiter)
                                        .setKeyResolver(userKeyResolver)
                                ))
                                .uri("lb://CART-SERVICE"))

                // ORDER SERVICE - Checkout Rate Limiting
                .route("order-checkout", r -> r
                                .path("/api/order/checkout")
                                .filters(f -> f.requestRateLimiter(c -> c
                                        .setRateLimiter(checkoutRateLimiter)
                                        .setKeyResolver(userKeyResolver)
                                ))
                                .uri("lb://ORDER-SERVICE"))

                // Remaining Order Management requests
                .route("order-service", r -> r
                                .path("/api/order/**")
                                .filters(f -> f.requestRateLimiter(c -> c
                                        .setRateLimiter(standardRateLimiter)
                                        .setKeyResolver(userKeyResolver)
                                ))
                                .uri("lb://ORDER-SERVICE"))

                // Restricted Payment Service Route
                .route("payment-service", r -> r
                                .path("/api/payment/webhook", "/api/payment/config")
                                .filters(f -> f.requestRateLimiter(c -> c
                                        .setRateLimiter(standardRateLimiter)
                                        .setKeyResolver(ipKeyResolver)
                                ))
                                .uri("lb://PAYMENT-SERVICE"))

                // Alias for payment config to bypass adblockers
                .route("payment-config-alias", r -> r
                                .path("/api/config/checkout-keys")
                                .filters(f -> f
                                        .rewritePath("/api/config/checkout-keys", "/api/payment/config")
                                        .requestRateLimiter(c -> c
                                                .setRateLimiter(standardRateLimiter)
                                                .setKeyResolver(ipKeyResolver)
                                        ))
                                .uri("lb://PAYMENT-SERVICE"))

                // Swagger Documentation Routes
                .route("swagger-user", r -> r.path("/USER-SERVICE/v3/api-docs").filters(f -> f.rewritePath("/USER-SERVICE/(?<segment>.*)", "/${segment}")).uri("lb://USER-SERVICE"))
                .route("swagger-book", r -> r.path("/BOOK-SERVICE/v3/api-docs").filters(f -> f.rewritePath("/BOOK-SERVICE/(?<segment>.*)", "/${segment}")).uri("lb://BOOK-SERVICE"))
                .route("swagger-review", r -> r.path("/REVIEW-SERVICE/v3/api-docs").filters(f -> f.rewritePath("/REVIEW-SERVICE/(?<segment>.*)", "/${segment}")).uri("lb://REVIEW-SERVICE"))
                .route("swagger-cart", r -> r.path("/CART-SERVICE/v3/api-docs").filters(f -> f.rewritePath("/CART-SERVICE/(?<segment>.*)", "/${segment}")).uri("lb://CART-SERVICE"))
                .route("swagger-order", r -> r.path("/ORDER-SERVICE/v3/api-docs").filters(f -> f.rewritePath("/ORDER-SERVICE/(?<segment>.*)", "/${segment}")).uri("lb://ORDER-SERVICE"))
                .route("swagger-payment", r -> r.path("/PAYMENT-SERVICE/v3/api-docs").filters(f -> f.rewritePath("/PAYMENT-SERVICE/(?<segment>.*)", "/${segment}")).uri("lb://PAYMENT-SERVICE"))
                .route("swagger-email", r -> r.path("/EMAIL-SERVICE/v3/api-docs").filters(f -> f.rewritePath("/EMAIL-SERVICE/(?<segment>.*)", "/${segment}")).uri("lb://EMAIL-SERVICE"))

                .build();
        }
}
/*
 * Federal Aviation Administration (FAA) public work 
 * 
 * As a work of the United States Government, this project is in the 
 * public domain within the United States. Additionally, we waive copyright 
 * and related rights in the work worldwide 
 * through the Creative Commons 0 (CC0) 1.0 Universal public domain dedication
 * 
 * APRA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */
package gov.faa.ait.apra.config;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityHeadersFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        exchange.getResponse().getHeaders().add("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        exchange.getResponse().getHeaders().add("X-Frame-Options", "DENY");
        exchange.getResponse().getHeaders().add("X-Content-Type-Options", "nosniff");
        exchange.getResponse().getHeaders().add("Content-Security-Policy", "default-src 'self'");
        exchange.getResponse().getHeaders().add("X-XSS-Protection", "1; mode=block");
        exchange.getResponse().getHeaders().add("Referrer-Policy", "strict-origin-when-cross-origin");
        exchange.getResponse().getHeaders().add("Permissions-Policy", "geolocation=(), microphone=(), camera=()");
        
        return chain.filter(exchange);
    }
}

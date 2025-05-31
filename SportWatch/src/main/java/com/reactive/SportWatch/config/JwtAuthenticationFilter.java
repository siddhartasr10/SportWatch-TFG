package com.reactive.SportWatch.config;

import java.util.List;
import java.util.logging.Logger;

import com.reactive.SportWatch.authentication.JwtAuthenticationToken;
import com.reactive.SportWatch.services.JwtService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

// Webflux no usa OncePerRequestFilter porque no usa httpSecurity pero ServerHttpSecurity
/**
 * Filtro que comprueba si el usuario tiene un JWT con JWS válido
 * en las cookies, la cookie del JWT se setea llamándose
 * authToken (puede ser lo que yo quiera)
 *
 */
//
@Component
public class JwtAuthenticationFilter implements WebFilter {

    @Autowired
    private JwtService jwtService;

    private static Logger logger = Logger.getLogger(JwtAuthenticationFilter.class.getName());
    /**
     *
     * @return Mono<Void> with
     *         {@code WebFilterChain.filter(ServerWebExchange exchange)}
     *         Delegates to the next filter with chain.filter
     *
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return jwtService.extractTokenFromCookies(exchange.getRequest().getCookies())
                .defaultIfEmpty("Invalid_Token")
                .flatMap(token -> {
                    if (token == null || token.equals("Invalid_Token")) {
                        // logger.info("INFO: User hasn't got a valid JWT");
                        return chain.filter(exchange);
                    }
                    return jwtService.getUsernameFromToken(token)
                        .map(username -> {
                                // logger.info("INFO: User has a valid JWT!");
                                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
                                Authentication auth = new JwtAuthenticationToken(token, username, authorities);
                                auth.setAuthenticated(true);
                                return new SecurityContextImpl(auth);

                        }).flatMap(ctx -> chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(ctx))));
                    });

    }
}

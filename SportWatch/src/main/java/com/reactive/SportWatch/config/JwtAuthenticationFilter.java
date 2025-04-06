package com.reactive.SportWatch.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

import com.reactive.SportWatch.authentication.JwtAuthenticationToken;
import com.reactive.SportWatch.services.JwtService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
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

    /**
     *
     * @return Mono<Void> with
     *         {@code WebFilterChain.filter(ServerWebExchange exchange)}
     *         Delegates to the next filter with chain.filter
     *
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = extractTokenFromCookies(exchange.getRequest().getCookies());

        if (Objects.nonNull(token)) {
            // Right now only one authority is used in my page, and is user.
            List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            Authentication auth = new JwtAuthenticationToken(token, jwtService.getUsernameFromToken(token),
                    authorities);
            // Make spring security know the guy is identified.
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        return chain.filter(exchange);

    }

    /**
     * Iterates the cookies {@link MultiValueMap} and then the authToken values:
     * {@code List<Cookies>}
     *
     * @param {@link MultiValueMap<String, HttpCookie>} cookies
     * @return {@link String} token  if one valid token is found. null if none valid found.
     *
     */
    public String extractTokenFromCookies(MultiValueMap<String, HttpCookie> cookies) {
        // cookies.forEach((key, values) -> { if (!key.equals("authToken")) continue;
        // for (HttpCookie cookie : values) { if
        // (!jwtService.validateToken(cookie.getValue())) continue; return true; } });
        for (Entry<String, List<HttpCookie>> multivalEntry : cookies.entrySet()) {
            if (!multivalEntry.getKey().equals("authToken")) continue;
            for (HttpCookie cookie : multivalEntry.getValue()) {
                if (!jwtService.validateToken(cookie.getValue())) continue;
                return cookie.getValue();
            }
        }
        return null;
        // cookies.forEach((key, values) -> { if (!key.equals("authToken")) continue;
        // for (HttpCookie cookie : values) { if
        // (!jwtService.validateToken(cookie.getValue())) continue; return true; } });
    }
}

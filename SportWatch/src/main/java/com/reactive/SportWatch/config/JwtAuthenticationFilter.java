package com.reactive.SportWatch.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.logging.Logger;

import com.reactive.SportWatch.authentication.JwtAuthenticationToken;
import com.reactive.SportWatch.services.JwtService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Flux;
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
        // logger.info("OLA?!?");
        return extractTokenFromCookies(exchange.getRequest().getCookies())
                .defaultIfEmpty("Invalid_Token")
                .flatMap(token -> {
                    if (token.equals("Invalid_Token")) return chain.filter(exchange);

                    return jwtService.getUsernameFromToken(token)
                        .map(username -> {
                                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
                                Authentication auth = new JwtAuthenticationToken(token, username, authorities);
                                return new SecurityContextImpl(auth);

                        }).flatMap(ctx -> chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(ctx))));
                    });


        // Synchronous code
        // String token = extractTokenFromCookies(exchange.getRequest().getCookies());

        // if (Objects.nonNull(token)) {
        //     // Right now only one authority is used in my page, and is user.
        //     List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        //     authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        //     Authentication auth = new JwtAuthenticationToken(token, jwtService.getUsernameFromToken(token), authorities);
        //     // Make spring security know the guy is identified.
        //     SecurityContextHolder.getContext().setAuthentication(auth);
        // }

        // return chain.filter(exchange);
    }

    /**
     * Iterates the cookies {@link MultiValueMap} and then the authToken values:
     * {@code List<Cookies>}
     *
     * @param {@link MultiValueMap<String, HttpCookie>} cookies
     * @return {@link Mono<String>} token  if one valid token is found. null if none valid found.
     *
     */
    public Mono<String> extractTokenFromCookies(MultiValueMap<String, HttpCookie> cookies) {
        List<HttpCookie> authCookies = cookies.get("authToken");
        if (Objects.isNull(authCookies) || authCookies.isEmpty()) {
            logger.info("No hay cookies de auth, asi que extractTokenFromCookies no puede hacer milagros");
            return Mono.empty();
        }

        return Flux.fromIterable(authCookies)
            .flatMap(cookie -> {
                    String token = cookie.getValue();
                    return jwtService.validateToken(token)
                            .filter(valid -> valid)
                            .map(valid -> token);

                }).next();

    }

        // return Mono.fromCallable(() -> {
        //     for (Entry<String, List<HttpCookie>> multivalEntry : cookies.entrySet()) {
        //         if (!multivalEntry.getKey().equals("authToken"))
        //             continue;
        //         for (HttpCookie cookie : multivalEntry.getValue()) {
        //             if (!(jwtService.validateToken(cookie.getValue()).block()))
        //                 continue;
        //             return cookie.getValue();
        //         }
        //     }
        //     return null;
        // }).subscribeOn(Schedulers.boundedElastic());

    // return Mono.justOrEmpty(cookies.get("authToken")) // Get the list of cookies for "authToken"
    //     .flatMap(cookieList -> {
    //         // For each cookie in the list, check if it's valid
    //         for (HttpCookie cookie : cookieList) {
    //             return jwtService.validateToken(cookie.getValue())
    //                     .filter(valid -> valid) // Filter out invalid tokens
    //                     .map(valid -> cookie.getValue()) // Return token value if valid
    //                     .switchIfEmpty(Mono.empty()); // If token is invalid, skip it
    //         }
    //         return Mono.empty(); // Return empty Mono if no valid token found
    //     });


        // for (Entry<String, List<HttpCookie>> multivalEntry : cookies.entrySet()) {
        //     if (!multivalEntry.getKey().equals("authToken")) continue;
        //     for (HttpCookie cookie : multivalEntry.getValue()) {
        //         if (!jwtService.validateToken(cookie.getValue())) continue;
        //         return cookie.getValue();
        //     }
        // }
        // return null;
    // }
}

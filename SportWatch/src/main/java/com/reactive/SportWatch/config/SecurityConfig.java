package com.reactive.SportWatch.config;

import java.util.UUID;

import com.reactive.SportWatch.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.WebFilter;

import reactor.core.publisher.Mono;


@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final ReactiveUserDetailsService customUserDetailService;

    private final PasswordEncoder passwordEncoder;
    // está en el mismo paquete "config" me ahorro el import.
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    public SecurityConfig(UserService customUserDetailService, PasswordEncoder passwordEncoder ,JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.customUserDetailService = customUserDetailService;
        this.passwordEncoder = passwordEncoder;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {

        http
            .addFilterBefore(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .authorizeExchange(exchanges -> exchanges
                               .pathMatchers("/api/register", "/api/login", "/").permitAll()
                               .anyExchange().authenticated())
            // .csrf(csrf -> csrf.csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse()))
            .csrf(csrf -> csrf.disable())
            // Con NoOP desactivo la session, lo que con el login por defecto hace que no sea capaz de autentificarme.
            .httpBasic(httpConf -> httpConf.securityContextRepository(NoOpServerSecurityContextRepository.getInstance()))
            .formLogin(login->login.disable());

        return http.build();
    }


    @Bean
    public ReactiveAuthenticationManager authenticationProvider() { // Sad DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(passwordEncoder());
        UserDetailsRepositoryReactiveAuthenticationManager authManager = new UserDetailsRepositoryReactiveAuthenticationManager(customUserDetailService);
        authManager.setPasswordEncoder(this.passwordEncoder);
        return authManager;
    }

    @Bean
    public WebFilter csrfTokenFilter() {
        return (exch, chain) -> {
            // HttpMethod Obj "matches" method is a String.equals not a regex match.
            if (exch.getRequest().getMethod().matches("GET")) {
            String csrfToken = UUID.randomUUID().toString();

            ResponseCookie cookie = ResponseCookie.from("XSRF-TOKEN", csrfToken)
            .httpOnly(false)
            .path("/")
            .sameSite("Lax")
            .build();

            exch.getResponse().addCookie(cookie);
            }
            return chain.filter(exch);
        };
    }


    @Bean
    public WebFilter csrfValidatorFilter() {
        return (exch, chain) -> {
            ServerHttpRequest request = exch.getRequest();
            if (request.getMethod().toString().matches("POST|PUT|DELETE")) {
                String headerToken = request.getHeaders().getFirst("X-XSRF-TOKEN");
                String cookieToken = (request.getCookies().getFirst("XSRF-TOKEN") != null) ?
                        request.getCookies().getFirst("XSRF-TOKEN").getValue() : null;

                if (headerToken == null || cookieToken == null || !headerToken.equals(cookieToken)) {
                    return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid CSRF token"));
                }

            };
            return chain.filter(exch);
        };
    }
}

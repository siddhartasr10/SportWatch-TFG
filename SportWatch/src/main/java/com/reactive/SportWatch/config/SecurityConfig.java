package com.reactive.SportWatch.config;

import java.util.UUID;
import java.util.logging.Logger;

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

    private static final Logger log = Logger.getLogger(SecurityConfig.class.getName());

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
                               .pathMatchers("/api/register", "/api/login", "/api/csrf-token", "/api/check-user", "/api/logout", "/feed", "/video/**", "/profile/**").permitAll()
                               .pathMatchers("/api/**").authenticated()
                               .anyExchange().permitAll())
            .httpBasic().disable()
            .formLogin(login -> login.loginPage("/login"))
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            // Con NoOP desactivo la session, lo que con el login por defecto hace que no sea capaz de autentificarme.
            .csrf(csrf -> csrf.disable());

        return http.build();
    }


    @Bean
    public ReactiveAuthenticationManager authenticationProvider() { // Sad DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(passwordEncoder());
        UserDetailsRepositoryReactiveAuthenticationManager authManager = new UserDetailsRepositoryReactiveAuthenticationManager(customUserDetailService);
        authManager.setPasswordEncoder(this.passwordEncoder);
        return authManager;
    }

    // Sets a csrf double submit type token on the user cookies only on get requests.
    @Bean
    public WebFilter csrfTokenFilter() {
        return (exch, chain) -> {
            // HttpMethod Obj "matches" method is a String.equals not a regex match.
            ServerHttpRequest request = exch.getRequest();

            String headerToken = request.getHeaders().getFirst("X-XSRF-TOKEN");
            String cookieToken = (request.getCookies().getFirst("XSRF-TOKEN") != null)
                ? request.getCookies().getFirst("XSRF-TOKEN").getValue()
                : null;

            if (exch.getRequest().getMethod().matches("GET") && cookieToken == null) {
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
                log.info("Header Token: " + headerToken);

                String cookieToken = (request.getCookies().getFirst("XSRF-TOKEN") != null)
                    ? request.getCookies().getFirst("XSRF-TOKEN").getValue()
                    : null;

                log.info("Cookie Token: " + cookieToken);

                log.info("Are they equals: " + headerToken.equals(cookieToken));
                if (headerToken == null || cookieToken == null || !headerToken.equals(cookieToken)) {
                    log.info("invalidtoken, also headerand cookies equal state: " + headerToken.strip().equals(cookieToken.strip()));
                    return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid CSRF token"));
                }

            };
            return chain.filter(exch);
        };
    }
}

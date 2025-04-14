package com.reactive.SportWatch.config;

import com.reactive.SportWatch.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
// import org.springframework.security.web.server.csrf.ServerCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;


@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final ReactiveUserDetailsService customUserDetailService;

    // está en el mismo paquete "config" me ahorro el import.
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    public SecurityConfig(UserService customUserDetailService, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.customUserDetailService = customUserDetailService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {

        http
            .addFilterBefore(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .authorizeExchange(exchanges -> exchanges
                               .pathMatchers("/**").permitAll()
                               .anyExchange().authenticated())
            .csrf(csrf -> csrf.csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse()))
            // Con NoOP desactivo la session, lo que con el login por defecto hace que no sea capaz de autentificarme.
            .httpBasic().and().securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            .formLogin(login->login.loginPage("/login"));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder;
    }

    @Bean
    public ReactiveAuthenticationManager authenticationProvider() { // Sad DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(passwordEncoder());
        UserDetailsRepositoryReactiveAuthenticationManager authManager = new UserDetailsRepositoryReactiveAuthenticationManager(customUserDetailService);
        authManager.setPasswordEncoder(passwordEncoder());
        return authManager;
    }


}

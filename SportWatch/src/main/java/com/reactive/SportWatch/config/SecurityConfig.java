package com.reactive.SportWatch.config;

import com.reactive.SportWatch.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
// import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;


/** TODO Jira issue: KAN-2 and KAN-3
* These are the objects which will be used for the KAN-2:
* {@link org.springframework.security.core.userdetails.UserDetailsService}
* {@link org.springframework.security.core.userdetails.ReactiveUserDetailsService}
* {@link org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService}
* These are the objects which will be used for the KAN-3:
* {@link org.springframework.security.authentication.ProviderManager}
* {@link org.springframework.security.authentication.dao.DaoAuthenticationProvider}
* or
* {@link org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder}
**/
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final UserService CustomUserDetailService;

    @Autowired
    public SecurityConfig(UserService CustomUserDetailService) {
        this.CustomUserDetailService = CustomUserDetailService;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .authorizeExchange(exchanges -> exchanges
                               .anyExchange().authenticated())
            .httpBasic().and()
            .formLogin();

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder;
    }

    @Bean
    public ReactiveAuthenticationManager authenticationProvider() { // Sad DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(passwordEncoder());
        UserDetailsRepositoryReactiveAuthenticationManager authManager = new UserDetailsRepositoryReactiveAuthenticationManager(CustomUserDetailService);
        authManager.setPasswordEncoder(passwordEncoder());
        return authManager;
    }

 // la configuracion implicita establece esto también pero si lo quieres modificar tienes que añadir esto.
}

package com.reactive.SportWatch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
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

    @Bean
    public MapReactiveUserDetailsService userDetailsService() {
        UserDetails user = User.builder() // para usar este usuario primero hay que configurar el token csrf
            .username("weakUser")
            .password("{noop}weak")
            .roles("USER")
            .build();

        return new MapReactiveUserDetailsService(user);
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .authorizeExchange(exchanges ->
                               exchanges
                    .anyExchange().authenticated()
            )
            .httpBasic().and()
            .formLogin();
        return http.build();
    }


 // la configuracion implicita establece esto también pero si lo quieres modificar tienes que añadir esto.
}

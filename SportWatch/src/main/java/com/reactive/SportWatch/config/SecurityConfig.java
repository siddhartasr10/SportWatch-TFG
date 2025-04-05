package com.reactive.SportWatch.config;

import com.reactive.SportWatch.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;


@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final ReactiveUserDetailsService CustomUserDetailService;

    //necesitaras algo de esto para el encoder/decoder a lo mejor no aquí pero en un jwt util pero por lo menos está a la vista
    private final JwtConfig jwtConfig;

    @Autowired
    public SecurityConfig(UserService CustomUserDetailService, JwtConfig jwtConfig) {
        this.CustomUserDetailService = CustomUserDetailService;
        this.jwtConfig = jwtConfig;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .authorizeExchange(exchanges -> exchanges
                               .pathMatchers("/login", "/logout", "/register").permitAll()
                               .anyExchange().authenticated())
            .httpBasic().and()
            .formLogin();

            // .httpBasic().and() .formLogin();

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


    // @Bean
    // public JwtDecoder jwtDecoder() {
    //     return NimbusJwtDecoder.withPublicKey(rsaKeyConfigProperties.publicKey()).build();
    // }

    // @Bean
    // JwtEncoder jwtEncoder() {
    //     JWK jwk = new RSAKey.Builder(rsaKeyConfigProperties.publicKey()).privateKey(rsaKeyConfigProperties.privateKey()).build();

    //     JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
    //     return new NimbusJwtEncoder(jwks);
    // }

}

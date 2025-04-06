package com.reactive.SportWatch.authentication;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    // El token
    private final String token;
    // El claim Sub (el usuario basicamente)
    private final String principal;

    // Sera "ROLE_USER" por el momento no va a haber otra.
    // podría usar el super constructor pero queda mas claro construirla aquí.
    //
    // private final Collection<GrantedAuthority> authorities = Collections.emptyList();
    // Collection ... va a ser literalmente ["ROLE_USER"]
    public JwtAuthenticationToken(String token, String principal, Collection <? extends GrantedAuthority> authorities) {
        super(authorities);
        this.token = token;
        this.principal = principal;
    }

    public String getPrincipal() {
        return principal;
    }

    public String getCredentials() {
        return token;
    }

}

package com.reactive.SportWatch.routes;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/api/logout")
public class LogoutController {

    @GetMapping()
    public Mono<ResponseEntity<String>> getLogout() {
        return Mono.just(ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build());
    }

    /**
     * Logout does not add JWT to blocklist, so user can still save that JWT
     * and use it after logout if it didn't expire.
     *
     * TODO: Implement blocklist, draft: "Implementar una blocklist de JWT".
     *
     * For tests token has to be valid, as {@link com.reactive.SportWatch.config.JwtAuthenticationFilter}
     * tests the token authenticity on each request.
     *
     */
    @PostMapping()
    public Mono<ResponseEntity<String>> postLogout(ServerWebExchange exch) {
        ResponseCookie jwtCookie = ResponseCookie.from("authToken").value(null)
                .httpOnly(true).maxAge(0)
                .sameSite("Strict").path("/")
                .secure(false).build();

        ResponseCookie usernameCookie = ResponseCookie.from("user").value(null)
                .httpOnly(false).maxAge(0)
                .sameSite("Strict").path("/")
                .secure(false).build();

        exch.getResponse().addCookie(usernameCookie);
        exch.getResponse().addCookie(jwtCookie);


        return Mono.just(ResponseEntity.ok("Logout handled succesfully"));
    }

}

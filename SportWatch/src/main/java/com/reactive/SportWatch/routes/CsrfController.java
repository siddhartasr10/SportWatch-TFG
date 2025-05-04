package com.reactive.SportWatch.routes;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import reactor.core.publisher.Mono;

// Temp controller for testing csrf token integration with angular
// Angular implements default support for detecting a xsrf cookie 'xsrf-token' and adding it
// to the request header as x-xsrf-token. But in dev server no request automatically adds the token
// is its made on angular dev server, so this endpoint is just for dev purposes only, so the proxied
// endpoints allowed by angular proxy can return a token to use for api interactions.

@Controller
@RequestMapping("/api/csrf-token")
public class CsrfController {

    // Filter automatically adds token to the cookies, so do nothing literally.
    @GetMapping("")
    public Mono<ResponseEntity<String>> getCsrfToken() {
        return Mono.just(ResponseEntity.ok("Here you have your token"));
    }

}

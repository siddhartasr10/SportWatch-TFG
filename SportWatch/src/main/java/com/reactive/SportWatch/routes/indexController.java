package com.reactive.SportWatch.routes;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

@Controller
public class indexController {

    @GetMapping("/")
    Mono<String> index() {
        // return Mono.just(ResponseEntity.ok("Hola"));
        return Mono.just("index");
    }
}

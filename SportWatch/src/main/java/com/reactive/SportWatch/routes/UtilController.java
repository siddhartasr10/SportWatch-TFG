package com.reactive.SportWatch.routes;

import com.reactive.SportWatch.services.JwtService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

import com.reactive.SportWatch.models.JsonResponse;

@RestController
@RequestMapping("/api/")
public class UtilController {

    JwtService jwtService;

    UtilController(JwtService jwtService) {
        this.jwtService = jwtService;
    }


    // Don't need to check jwt as route is jwt protected.
    // Returns {msg: "username"}
    @GetMapping("check-user")
    Mono<JsonResponse> checkUser(ServerWebExchange exch) {
        return jwtService.extractTokenFromCookies(exch.getRequest().getCookies())
                .flatMap(token -> jwtService.getUsernameFromToken(token))
                .map(username -> new JsonResponse(username));
        }

}

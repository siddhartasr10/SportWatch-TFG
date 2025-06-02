package com.reactive.SportWatch.routes;

import java.util.Map;

import com.reactive.SportWatch.models.ExtUserDetails;
import com.reactive.SportWatch.models.JsonResponse;
import com.reactive.SportWatch.services.JwtService;
import com.reactive.SportWatch.services.UserService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/")
public class UtilController {

    JwtService jwtService;
    UserService userService;

    UtilController(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }


    // Don't need to check jwt as route is jwt protected.
    // Returns {msg: "username"}
    @GetMapping("check-user")
    Mono<JsonResponse> checkUser(ServerWebExchange exch) {
        return jwtService.extractTokenFromCookies(exch.getRequest().getCookies())
                .flatMap(token -> jwtService.getUsernameFromToken(token))
                .map(username -> new JsonResponse(username));
    }

    @GetMapping("user/{username}")
    Mono<ExtUserDetails> getUserByUsername(@PathVariable String username) {
        return userService.findAllByUsername(username);
    }

    @GetMapping("followers/{username}")
    Flux<Map<String,Object>> getFollowersOfUsername(@PathVariable String username) {
        return userService.findFollowersOfUsername(username);

    }

    @GetMapping("suscribers/{username}")
    Flux<Map<String,Object>> getSuscribersOfUsername(@PathVariable String username) {
        return userService.findSuscribersOfUsername(username);
    }

    @GetMapping("follows/{username}")
    Flux<Map<String,Object>> getUsernameFollows(@PathVariable String username) {
        return userService.findUsernameFollows(username);
    }


    @GetMapping("suscribed/{username}")
    Flux<Map<String,Object>> getUsernameSuscribed(@PathVariable String username) {
        return userService.findUsernameSuscribed(username);
    }

}

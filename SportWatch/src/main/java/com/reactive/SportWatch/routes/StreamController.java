package com.reactive.SportWatch.routes;

import com.reactive.SportWatch.models.JsonResponse;
import com.reactive.SportWatch.services.JwtService;
import com.reactive.SportWatch.services.StreamService;
import com.reactive.SportWatch.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/")
public class StreamController {

    private StreamService streamService;
    private JwtService jwtService;
    private UserService userService;

    @Autowired
    StreamController(StreamService streamService, JwtService jwtService, UserService userService) {
        this.streamService = streamService;
        this.jwtService = jwtService;
        this.userService = userService;
    }


    @PostMapping("view/{stream_id}")
    Mono<JsonResponse> viewStream(@PathVariable Integer stream_id, ServerWebExchange exch) {
        return jwtService.extractTokenFromCookies(exch.getRequest().getCookies())
            .flatMap(token -> jwtService.getUsernameFromToken(token))
            .flatMap(username -> userService.findIdByUsername(username))
            .flatMap(userId -> streamService.viewAStream(stream_id, userId))
            .map(nothing -> new JsonResponse("Stream with id: " + stream_id + "Viewed Successfully"));

    }

    @GetMapping("stream/{stream_id}/views")
    Mono<Integer> getStreamViews(@PathVariable Integer stream_id) {
        return streamService.countStreamViews(stream_id);
    }


}

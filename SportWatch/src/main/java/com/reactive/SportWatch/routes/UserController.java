package com.reactive.SportWatch.routes;

import java.util.Map;
import java.util.logging.Logger;

import com.reactive.SportWatch.models.ExtUserDetails;
import com.reactive.SportWatch.models.JsonResponse;
import com.reactive.SportWatch.services.JwtService;
import com.reactive.SportWatch.services.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/")
public class UserController {

    JwtService jwtService;
    UserService userService;

    private static Logger log = Logger.getLogger(UserController.class.getName());

    UserController(JwtService jwtService, UserService userService) {
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

    @PostMapping("follow/{follower}/{streamer}")
    public Mono<Integer> followUser(@PathVariable String follower, @PathVariable String streamer, ServerWebExchange exch) {
        return jwtService.extractTokenFromCookies(exch.getRequest().getCookies())
                .flatMap(token -> jwtService.getUsernameFromToken(token))
                // .map(username -> {log.info("Username found in the token: " + username); return username;})
                .doOnNext(username -> log.info("The username, from the controller: " + username))
                .map(username -> username.equals(follower))
                .filter(equalsFollower -> equalsFollower)
                .flatMap(userIsFollower -> userService.followUser(follower, streamer))
                .doOnNext(changes -> log.info("changes on followUser: " + changes))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You cannot follow a streamer if you aren't the user following!")));
    }

    @PostMapping("suscribe/{suscriber}/{streamer}")
    public Mono<Integer> suscribeUser(@PathVariable String suscriber, @PathVariable String streamer, ServerWebExchange exch) {
        return jwtService.extractTokenFromCookies(exch.getRequest().getCookies())
            .flatMap(token -> jwtService.getUsernameFromToken(token))
                .filter(username -> username.equals(suscriber))
                .flatMap(userIsFollower -> userService.suscribeUser(suscriber, streamer))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You cannot suscribe to a streamer if you aren't the user that's suscribing!")));
    }

    @DeleteMapping("unfollow/{follower}/{streamer}")
    public Mono<Integer> unfollowUser(@PathVariable String follower, @PathVariable String streamer, ServerWebExchange exch) {
        return jwtService.extractTokenFromCookies(exch.getRequest().getCookies())
                .flatMap(token -> jwtService.getUsernameFromToken(token))
                .filter(username -> username.equals(follower))
                .flatMap(userIsFollower -> userService.unfollowUser(follower, streamer))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You cannot unfollow a streamer if you aren't the user following!")));
    }

    @DeleteMapping("unsuscribe/{suscriber}/{streamer}")
    public Mono<Integer> unsuscribeUser(@PathVariable String suscriber, @PathVariable String streamer, ServerWebExchange exch) {
        return jwtService.extractTokenFromCookies(exch.getRequest().getCookies())
                .flatMap(token -> jwtService.getUsernameFromToken(token))
                .filter(username -> username.equals(suscriber))
                .flatMap(userIsFollower -> userService.unsuscribeUser(suscriber, streamer))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You cannot unsuscribe to a streamer if you aren't the user that's suscribing!")));
    }

    @GetMapping("follows/{follower}/{streamer}")
    public Mono<Boolean> checkFollows(@PathVariable String follower, @PathVariable String streamer, ServerWebExchange exch) {
        return jwtService.extractTokenFromCookies(exch.getRequest().getCookies())
                .flatMap(token -> jwtService.getUsernameFromToken(token))
                .filter(username -> username.equals(follower))
                .flatMap(userIsFollower -> userService.checkUserFollowsStreamerByUsername(follower, streamer))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You cannot check if a user follows a streamer if you aren't the user that's following!")));
    }

    @GetMapping("suscribes/{suscriber}/{streamer}")
    public Mono<Boolean> checkSuscribed(@PathVariable String suscriber, @PathVariable String streamer, ServerWebExchange exch) {
        return jwtService.extractTokenFromCookies(exch.getRequest().getCookies())
                .flatMap(token -> jwtService.getUsernameFromToken(token))
                .filter(username -> username.equals(suscriber))
                .flatMap(userIsFollower -> userService.checkUserSuscribedStreamerByUsername(suscriber, streamer))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You cannot check if a user is suscribed to a streamer if you aren't the user that's suscribed!")));
    }

}

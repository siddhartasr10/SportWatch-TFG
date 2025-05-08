package com.reactive.SportWatch.routes;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.reactive.SportWatch.models.ExtUser;
import com.reactive.SportWatch.models.ExtUserDetails;
import com.reactive.SportWatch.services.JwtService;
import com.reactive.SportWatch.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/api/register")
public class RegisterController {

    private static final Logger logger = Logger.getLogger(RegisterController.class.getName());

    private final JwtService jwtService;

    private final ReactiveAuthenticationManager authManager;

    private final UserService userService;

    private final String invalidUser = "iXoTDoUQTUAmvjzmuvivhKztuZNgzhMdmfrwOXJsUHthxHkeffS";

    // Don't need to add SecurityConfig to get the authManager
    @Autowired
    RegisterController(JwtService jwtService, ReactiveAuthenticationManager authManager, UserService userService) {
        this.jwtService = jwtService;
        this.authManager = authManager;
        this.userService = userService;
    }

    @GetMapping()
    public Mono<ResponseEntity<String>> getRegister() {
        return Mono.just(ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build());
    }

    // We asume field validation is made on frontend by Angular
    @PostMapping()
    public Mono<ResponseEntity<String>> postRegister(ServerWebExchange exch) {
        // Creo el token antes de que el usuario exista. Porque puedo.

        return exch.getFormData().flatMap(formData -> {
            if (exch.getRequest().getHeaders().get("Content-Type")
                .toString().startsWith("[multipart/form-data"))

                return Mono.error(
                        new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "You're using multipart/form-data instead of application/x-www-form-urlencoded,"
                                + "that's not supported sorry."));

            return extractUserFromFormData(formData);
            })
            .flatMap(user -> {
                Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword(), authorities);
                return authManager.authenticate(authenticationToken)
                    .defaultIfEmpty(new UsernamePasswordAuthenticationToken(this.invalidUser, this.invalidUser, authorities))
                      // Default cannot be null if I want to check it on flatmap, cause map ignores null values.
                    .flatMap(auth -> {
                            // If invalid user set by empty I return the reason authManager says.
                            return (auth.getPrincipal().toString().length() == 51)
                                ? Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization couldn't be determined"))
                                : Mono.just(auth);
                    })
                    .doOnError(err -> logger.log(Level.WARNING, "Authentication error!:", err))

                    .flatMap(auth ->jwtService.generateToken(user.getUsername()).flatMap(token -> jwtService.setUserCookies(token, user.getUsername(), exch)))
                    .flatMap(cookies -> Mono.just(ResponseEntity.status(HttpStatus.ACCEPTED).body("Authentication succeded, use authToken and user cookies pls")));
            });



    }

    private Mono<ExtUserDetails> extractUserFromFormData(MultiValueMap<String, String> formData) {
        String username = formData.getFirst("username");
        String password = formData.getFirst("password");
        String email = formData.getFirst("email");

        logger.info(String.format("Entire map: %s", formData));

        logger.info(username+","+password);
        if (username == null || password == null)
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username and password must be set in the request"));

        return userService.createUser(ExtUser.extBuilder()
                .username(username)
                .password(password)
                .email(email)
                .build());
    }


}

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
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;
@Controller
@RequestMapping("/api/")
public class AuthController {

    private static final Logger logger = Logger.getLogger(AuthController.class.getName());

    private final JwtService jwtService;

    private final ReactiveAuthenticationManager authManager;

    private final UserService userService;
    // 51 length username (max is 50) I will be using in case empty mono happens, bc null cannot be used in a mono.
    private final String invalidUser = "iXoTDoUQTUAmvjzmuvivhKztuZNgzhMdmfrwOXJsUHthxHkeffS";

    // Don't need to add SecurityConfig to get the authManager
    @Autowired
    AuthController(JwtService jwtService, ReactiveAuthenticationManager authManager, UserService userService) {
        this.jwtService = jwtService;
        this.authManager = authManager;
        this.userService = userService;
    }

    @GetMapping("register")
    public Mono<ResponseEntity<String>> getRegister() {
        return Mono.just(ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build());
    }

    // We asume field validation is made on frontend by Angular
    @PostMapping("register")
    public Mono<ResponseEntity<String>> postRegister(ServerWebExchange exch) {
        // Creo el token antes de que el usuario exista. Porque puedo.

        return exch.getFormData().flatMap(formData -> {
            if (exch.getRequest().getHeaders().get("Content-Type")
                .toString().startsWith("[multipart/form-data"))

                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
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
                // I could use ((Userdetails) auth.getPrincipal()).getUsername() and avoid chaining to the flatmap of user but lines would be longer
                .flatMap(auth -> jwtService.generateToken(user.getUsername()).flatMap(token -> jwtService.setUserCookies(token, user.getUsername(), exch)))
                .flatMap(cookies -> Mono.just(ResponseEntity.status(HttpStatus.ACCEPTED).body("Authentication succeded, use authToken and user cookies pls")));
        });



    }

    @GetMapping("login")
    public Mono<ResponseEntity<String>> getLogin() {
        return Mono.just(ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build());
    }

    /**   Takes some username and password, authenticates in the security context and generates a JWT token which returns in the Authtok
    *     We asume field validation is made on frontend by Angular
    *     Postman es un cabrón y manda los datos en multipart/form-data en vez de application/x-www-form-urlencoded
    *     Especifica en postman que quieres el segundo, porque el primero es mas ineficiente y además
    *     Incómodo de usar de cojones. (((FormFieldPart) Part).value() noseque vs String noseque dime tu cual es mejor)
    *     No se puede usar @RequestParameters porque en webflux solo pilla query params.
    */
    @PostMapping("login")
    public Mono<ResponseEntity<String>> postLogin(ServerWebExchange exch) {

        return exch.getFormData().flatMap(formData -> {
            if (exch.getRequest().getHeaders().get("Content-Type")
                .toString().startsWith("[multipart/form-data"))

            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                                          "You're using multipart/form-data instead of application/x-www-form-urlencoded,"
                                                          + "that's not supported sorry."));

            String username = formData.getFirst("username"), password = formData.getFirst("password");

            if (username == null || password == null)
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username and password must be set in the request"));

            // No lo añado al contexto porque no hace falta, para nada autentificar esta request. El JWT generado ya me sirve en futuras requests.
            Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password, authorities);

            return authManager.authenticate(authenticationToken) // cannot be null if I want to check it on flatmap, cause map ignores null values.
                    .defaultIfEmpty(new UsernamePasswordAuthenticationToken(this.invalidUser, this.invalidUser, authorities))
                    .flatMap(auth -> {
                            // If invalid user set by empty I return the reason authManager says for why it returns empty.
                            return (auth.getPrincipal().toString().length() == 51)
                                ? Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization couldn't be determined"))
                                : Mono.just(auth);
                    })
                    .doOnError(err -> logger.warning("Authentication error!"));
            })
            // Default authManager uses {@link UserDetails} as principal of the Authentication. in this case (login is a longer func) i prefer using it so func is clearer.
            .flatMap(auth -> jwtService.generateToken(((UserDetails) auth.getPrincipal()).getUsername())
                     .flatMap(token -> jwtService.setUserCookies(token, ((UserDetails) auth.getPrincipal()).getUsername(), exch)))

            .flatMap(cookies -> Mono.just(ResponseEntity.status(HttpStatus.ACCEPTED)
                                          .body("Authentication succeded, use authToken and user cookies pls")));

    }

    @GetMapping("logout")
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
    @PostMapping("logout")
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


    private Mono<ExtUserDetails> extractUserFromFormData(MultiValueMap<String, String> formData) {
        String username = formData.getFirst("username"), password = formData.getFirst("password");
        String email = formData.getFirst("email");

        // logger.info(String.format("Entire map: %s", formData));
        // logger.info(username+","+password);

        if (username == null || password == null)
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username and password must be set in the request"));

        return userService.createUser(ExtUser.extBuilder()
                .username(username)
                .password(password)
                .email(email)
                .build());
    }


}

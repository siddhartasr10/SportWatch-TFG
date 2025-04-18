package com.reactive.SportWatch.routes;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import com.reactive.SportWatch.services.JwtService;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/api/login")
public class LoginController {

    private final ReactiveAuthenticationManager authManager;

    private final JwtService jwtService;

    // 51 length username (max is 50) I will be using in case empty mono happens, bc null cannot be used in a mono.
    private final String invalidUser = "iXoTDoUQTUAmvjzmuvivhKztuZNgzhMdmfrwOXJsUHthxHkeffS";

    private final static Logger logger = Logger.getLogger(LoginController.class.getName());

    // I don't need to inject SecurityConfig and then call the func, authManager is a Bean already.
    @Autowired
    LoginController(JwtService jwtService, ReactiveAuthenticationManager authManager) {
        this.jwtService = jwtService;
        this.authManager = authManager;
    }

    @GetMapping()
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
    @PostMapping()
    public Mono<ResponseEntity<String>> postLogin(ServerWebExchange exch) {

        return exch.getFormData().flatMap(formData -> {
            if (exch.getRequest().getHeaders().get("Content-Type").toString().startsWith("[multipart/form-data"))
                return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body("You're using multipart/from-data instead of application/x-www-form-urlencoded, its not supported sorry."));

            String username = formData.getFirst("username");
            String password = formData.getFirst("password");

            if (username == null || password == null)
                return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body("Username and password must be set in the request"));

            // No lo añado al contexto porque no hace falta, para nada autentificar esta request. El JWT generado ya me sirve en futuras requests.
            Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password, authorities);

            return authManager.authenticate(authenticationToken)
                // cannot be null if I want to check it on flatmap, cause map ignores null values.
                    .defaultIfEmpty(new UsernamePasswordAuthenticationToken(this.invalidUser, this.invalidUser, authorities))
                    .flatMap(auth -> {
                            // If invalid user set by empty I return the reason authManager says for why it returns empty.
                            return (auth.getPrincipal().toString().length() == 51)
                                ? Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization couldn't be determined"))
                                : Mono.just(auth);
                    })
                    .doOnError(err -> logger.warning("Authentication error!"))
                    .flatMap(auth -> {

                        return jwtService.generateToken(username).flatMap(token -> {

                            jwtService.setUserCookies(token, username, exch);

                            return Mono.just(ResponseEntity.status(HttpStatus.ACCEPTED)
                                    .body("Authentication succeded, use authToken and user cookies pls"));

                        });
                    });
        });

    }
}

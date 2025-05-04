package com.reactive.SportWatch.services;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import com.reactive.SportWatch.config.JwtConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import reactor.core.publisher.Mono;

@Service
public class JwtService {

    private final JwtConfig jwtConfig;

    @Autowired
    public JwtService(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    private static Logger logger = Logger.getLogger(JwtService.class.toString());

    // Duration in days
    private static final Integer TOKEN_DURATION = 7;

    public Mono<String> generateToken(String username) throws IllegalStateException {

        return Mono.fromCallable(() -> Jwts.builder().subject(username)
                .issuedAt(new Date()) // El tiempo de expiración aquí es en milisegundos (Date usa milisegundos)
                .expiration(new Date(new Date().getTime() + 1000 * 60 * 60 * 24 * TOKEN_DURATION))
                // Por defecto usa de digest algorithm SHA256, así que el "alg" es RS256, pero
                // lo pone automaticamente la libreria.
                .signWith(jwtConfig.privateKey())
                // No es necesario, es opcional añadir el tipo pero bueno yo lo pongo
                .header().type("JWT")
                .and().compact());

    }

    // verifyWith throws JwtException which encapsulates:
        // SignatureException: Thrown when the signature doesn’t match.
        // ExpiredJwtException: Thrown when the token is expired.
        // PrematureJwtException: Thrown when the token isn’t yet valid (e.g., nbf
        // claim).
        // MalformedJwtException: Thrown when the JWT structure is invalid.
    public Mono<String> getUsernameFromToken(String token) throws IllegalStateException {
        try {
            return Mono.just(Jwts.parser().verifyWith(jwtConfig.publicKey())
                    .build().parseSignedClaims(token)
                    .getPayload().getSubject());

        } catch (JwtException e) {
            logger.warning(String.format("JWT exception during validation: %s", (Object) e.getStackTrace()));
            return Mono.empty();
        }
    }

    // Verify the signature and the username, verifyWith throws JwtException
    // the signature is verified when username gets extracted
    // username is null when JWTexception throws, so invalid signature for example
    // @returns true if token is valid or false if its not
    public Mono<Boolean> validateToken(String token) {
        if (Objects.isNull(token)) return Mono.just(false);
        return getUsernameFromToken(token)
                .filter(username -> Objects.nonNull(username))
                // .doOnNext(username -> logger.info(String.format("JWT username: %s", username)))
                .flatMap(validUsername -> isTokenExpired(token))
                // .doOnNext(isExpired -> logger.info(String.format("Is JWT expired?: %s", isExpired)))
                .map(isExpired -> !isExpired) // this should be a filter, but for some reason it doesn't work right if its one.
                // Invert the cond, if is expired then validateToken should return false (as token is not valid)
                .defaultIfEmpty(false);
                // .doOnSuccess(res -> logger.info(String.format("Is token valid?: %s", res)));

    }

    // if no expiration date found the token is permannent
    private Mono<Boolean> isTokenExpired(String token) {
        Mono<Date> expiration = getExpirationDateFromToken(token);

        return expiration.flatMap((date) ->
            (Objects.isNull(date))
                ? Mono.just(false)
                : Mono.just(date.before(new Date()) || date.equals(new Date()))
               // .doOnSuccess(res -> logger.info(String.format("Is token expired?: %s", res)))
            // If expiration date is earlier than right now or right now then its expired.
        );

    }

    private Mono<Date> getExpirationDateFromToken(String token) {
        return Mono.fromCallable(() -> Jwts.parser()
                .verifyWith(jwtConfig.publicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration());

    }
    /**
     * <h1> Create main user cookies </h1>
     * <p> Utility method to quickly create and add common auth cookies to the response </p>
     * <p> "authToken" and "user" get created by this method </p>
     * maxAge is 7 days, if needed a parameter can be added or if wanted longer time but not variations, <br>
     * the constant of this service can be changed {@code JwtService.TOKEN_DURATION}
     * @param token {@link String} the JWT
     * @param username {@link String}
     * @param exch {@link ServerWebExchange} Server exchange in which response the cookies are going to be set in.
     * <br>
     * @implNote
     * Intended to use like {@code jwtService.generateToken().then(n -> jwtService.setUserCookies())} <br>
     * or with other continuators like {@code Mono.doOnSuccess}
     *
     *  */
    public Mono<List<ResponseCookie>> setUserCookies(String token, String username, ServerWebExchange exch) {

        // maxAge should be == jwt Expiration time, Secure should be true when in prod.
        // Maybe samesite will change when aws streaming
        // Time here is in seconds.

        ResponseCookie jwtCookie = ResponseCookie.from("authToken").value(token)
                .httpOnly(true).maxAge(3600L * 24 * JwtService.TOKEN_DURATION)
                .sameSite("Strict").path("/")
                .secure(false).build();

        // as jwtCookie is httpOnly this one is to get the username for rendering
        // purposes in Angular
        ResponseCookie usernameCookie = ResponseCookie.from("user").value(username)
                .httpOnly(false).maxAge(3600L * 24 * JwtService.TOKEN_DURATION)
                .sameSite("Strict").path("/")
                .secure(false).build();

        exch.getResponse().addCookie(jwtCookie);
        exch.getResponse().addCookie(usernameCookie);

        return Mono.just(List.of(jwtCookie, usernameCookie));
    }
}

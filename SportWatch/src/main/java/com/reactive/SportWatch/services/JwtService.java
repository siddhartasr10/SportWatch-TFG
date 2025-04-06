package com.reactive.SportWatch.services;

import java.util.Date;
import java.util.Objects;
import java.util.logging.Logger;

import com.reactive.SportWatch.config.JwtConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;


@Service
public class JwtService {

    private final JwtConfig jwtConfig;

    @Autowired
    public JwtService(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    private static Logger logger = Logger.getLogger(JwtService.class.toString());

    public String generateToken(String username, Integer daysToExpire) throws IllegalStateException {
        return Jwts.builder().subject(username)
                .issuedAt(new Date()) // Dos semanas en milisegundos -> expira en 2 semanas
                .expiration(new Date(new Date().getTime() + 1000 * 60 * 60 * 24 * daysToExpire))
                // Por defecto usa de digest algorithm SHA256, así que el "alg" es RS256, pero lo pone automaticamente la libreria.
                .signWith(jwtConfig.privateKey())
                // No es necesario, es opcional añadir el tipo pero bueno yo lo pongo
                .header().type("JWT")
                .and().compact();
    }

    // verifyWith throws JwtException which encapsulates:
    // SignatureException: Thrown when the signature doesn’t match.
    // ExpiredJwtException: Thrown when the token is expired.
    // PrematureJwtException: Thrown when the token isn’t yet valid (e.g., nbf
    // claim).
    // MalformedJwtException: Thrown when the JWT structure is invalid.
    public String getUsernameFromToken(String token) throws IllegalStateException {
        try {
            return Jwts.parser().verifyWith(jwtConfig.publicKey())
                    .build().parseSignedClaims(token)
                    .getPayload().getSubject();
        } catch (JwtException e) {
            logger.warning(String.format("JWT exception during validation: %s", (Object) e.getStackTrace()));
            return null;
        }
    }
    // Verify the signature and the username, verifyWith throws JwtException
    // the signature is verified when username gets extracted
    // username is null when JWTexception throws, so invalid signature for example
    public boolean validateToken(String token) {
        if (Objects.isNull(token)) return false;
        String extractedUsername = getUsernameFromToken(token);
        // if a username exists but signature is invalid, it will be null
        return (!isTokenExpired(token) && Objects.nonNull(extractedUsername));
    }

    // if no expiration date found the token is permannent
    private boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);

        if (Objects.isNull(expiration)) return false;
        // if expires earlier than right now or right now then its expired.
        return expiration.before(new Date()) || expiration.equals(new Date());
    }

    private Date getExpirationDateFromToken(String token) {
        return Jwts.parser()
                .verifyWith(jwtConfig.publicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
    }
}

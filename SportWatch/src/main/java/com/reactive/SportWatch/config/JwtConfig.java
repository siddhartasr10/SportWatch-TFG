package com.reactive.SportWatch.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.logging.Logger;

// import java.security.spec.RSAPublicKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class JwtConfig {
    // Mejor classpath que path absoluto, si cambio el proyecto de sitio no se ve afectado.
    @Value("${jwt.public-key-path}")
    private String publicKeyClassPath;

    @Value("${jwt.private-key-path}")
    private String privateKeyClassPath;

    private static final Logger logger = Logger.getLogger(JwtConfig.class.toString());

    @Bean
    public RSAPublicKey publicKey() {
        try {
        Path path = new ClassPathResource(publicKeyClassPath).getFile().toPath();
        byte[] keyBytes = Files.readAllBytes(path);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        }

        catch (IOException err) {
            logger.warning(String.format("An IO error ocurred retrieving the JWS Public Key: %s", (Object) err.getStackTrace()));
        }

        catch (NoSuchAlgorithmException err) {
            logger.warning(String.format("An error ocurred initializing the JWS Public Key java KeyFactory Instance: %s", (Object) err.getStackTrace()));
        }

        catch (InvalidKeySpecException err) {
            logger.warning(String.format("An error ocurred creating the JWS Public Key object: %s", (Object) err.getStackTrace()));
        }

        throw new IllegalStateException("Public Key wasn't retrieved correctly");
    }

    @Bean
    public RSAPrivateKey privateKey() {
        try {
        Path path = new ClassPathResource(privateKeyClassPath).getFile().toPath();
        byte[] keyBytes = Files.readAllBytes(path);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        }

        catch (IOException err) {
            logger.warning(String.format("An IO error ocurred retrieving the JWS Private Key: %s", (Object) err.getStackTrace()));
        }

        catch (NoSuchAlgorithmException err) {
            logger.warning(String.format("An error ocurred initializing the JWS Private Key java KeyFactory Instance: %s", (Object) err.getStackTrace()));
        }

        catch (InvalidKeySpecException err) {
            logger.warning(String.format("An error ocurred creating the JWS Private Key object: %s", (Object) err.getStackTrace()));
        }

        throw new IllegalStateException("Private Key wasn't retrieved correctly");
    }
}

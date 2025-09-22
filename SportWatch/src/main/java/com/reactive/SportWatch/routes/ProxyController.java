package com.reactive.SportWatch.routes;

import java.net.MalformedURLException;
import java.util.logging.Logger;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/** @desc class created to forward aws ivs streams
 * As IVS doesn't have a way to configure CORS without cdn's
 * */
@RestController
public class ProxyController {

    Logger log = Logger.getLogger(ProxyController.class.getCanonicalName());
    final String IvsBaseUrl = "https://streams-ivs.s3.eu-west-1.amazonaws.com";


    @GetMapping("/stream-proxy/{*path}")
    Mono<Resource> getStream(@PathVariable String path) throws MalformedURLException {
        // System.out.println(path); log.info("IvsBaseUrl " + IvsBaseUrl); log.info("Stream proxy path: " + IvsBaseUrl + path); UrlResource stream = new UrlResource(IvsBaseUrl + path);
        return Mono.just(new UrlResource(IvsBaseUrl + path));
    }

}

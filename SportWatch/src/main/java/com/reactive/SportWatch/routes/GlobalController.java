package com.reactive.SportWatch.routes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * <br>
 * <p> Matches with everything with the minimum priority (other controllers match earlier). </p>
 *
 * @hidden
 * No puedo usar regex por desgracia, ya que solo permite regex por sección <br>
 * es decir, por cada una de las {@code '/'} ej. "localhost/ Seccion / Seccion" <br>
 * para afectar a todas necesito un wildcard, que es lo que he acabado usando.
 *
 *  */
@RestController
public class GlobalController {

    @GetMapping("/{*path}")
    Mono<ResponseEntity<Resource>> defaultRouter(@PathVariable String path, ServerWebExchange exch) {

        final Resource ENTRYPOINT = new ClassPathResource("static/index.html");

        if (Pattern.compile("\\.(?:js|css|html|ico|img)$").matcher(path).find()) {

            Resource source = extractResourceFromPath(path);
            return Mono.just(ResponseEntity.ok(source));
        }

        /* Logica para añadir manualmente el token a la solicitud (al final hice un filtro que esta en securityConfig)
              Mono<CsrfToken> csrfToken = exch.getAttribute(CsrfToken.class.getName());
              return csrfToken.doOnNext(token -> System.out.println(token.getToken())) .then */

        return (Mono.just(ResponseEntity.ok(ENTRYPOINT)));
    }

    public ClassPathResource extractResourceFromPath(String path) {

        // System.out.println(path);
        try {
            Matcher matcher = Pattern.compile("[/\\w|\\d-]*\\.[html|css|js|img|ico]+").matcher(path); matcher.find();
            String filePath = matcher.group();

        // System.out.println(filePath);

            ClassPathResource source = new ClassPathResource("static/" + filePath);
            if (!source.exists() || !source.isFile() || !source.isReadable()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No file: " + filePath + " was found");

            return source;
        }
        catch (IllegalStateException e) {throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Regex pattern for " + path + " failed trying to match a common file extension... (no match found)");}
        catch (IndexOutOfBoundsException e) {throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Regex pattern for " + path + " failed trying to match a common file extension... (index out of bounds in the Matcher)");}
    }

}

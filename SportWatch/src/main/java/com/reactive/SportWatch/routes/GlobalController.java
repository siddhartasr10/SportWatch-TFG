package com.reactive.SportWatch.routes;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    Logger log = Logger.getLogger(GlobalController.class.getName());

    @GetMapping("/{*path}")
    Mono<ResponseEntity<Resource>> defaultRouter(@PathVariable String path, ServerWebExchange exch) {

        final Resource ENTRYPOINT = new ClassPathResource("static/index.html");

        if (Pattern.compile("\\.(?:html|css|js|img|ico|png|jpeg|woff2|svg)$").matcher(path).find()) {

            Resource source = extractResourceFromPath(path);

            // Los svgs no se pueden ver en la página cuando se mandan como MEDIA text/html, necesita su media especial.
            return (path.endsWith(".svg"))
                ? Mono.just(ResponseEntity.ok().contentType(MediaType.valueOf("image/svg+xml")).body(source))
                : Mono.just(ResponseEntity.ok(source));

        }

        return Mono.just(ResponseEntity.ok(ENTRYPOINT));
    }

    // @GetMapping(value = {"/**/{path:*\\.svg$}", "/{path:*\\.svg$}"})



    /**
     * @deprecated Antes la usaba para todos los archivos, pero servirlos manualmente con ClassPath da problemas dentro de un jar. <br>
     * Curiosamente no con string literales pero si con paths de variables (lo cual es un problema jajajaj) <br>
     * Porque a veces under the hood se convertirá en inputStream y otras en File -> File.toString y cuando lo hace a File <br>
     * pues falla por el mismo motivo que fallaba jwtConfig y AwsConfig. <br>
     *
     * @param path String
     * @return ClassPathResource
     */

    public ClassPathResource extractResourceFromPath(String path) {

        // System.out.println(path);
        try {
            Matcher matcher = Pattern.compile("[/\\w|\\d-]*\\.[html|css|js|img|ico|png|jpeg|woff2|svg]+").matcher(path); matcher.find();
            String filePath = matcher.group();

            // log.info("Recurso solicitado: " + "static" + filePath);
            // System.out.println("static" + filePath);

            // todos los path empiezan por una /
            String finalPath = "static" + filePath;


            ClassPathResource source = new ClassPathResource(finalPath);
            // !source.isFile() || !source.exists() || source is file no se puede usar en un jar.
            if (!source.isReadable()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No file: " + filePath + " was found");

            // log.info("Recurso recuperado: " + source.toString());

            return source;
        }
        catch (IllegalStateException e) {throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Regex pattern for " + path + " failed trying to match a common file extension... (no match found)");}
        catch (IndexOutOfBoundsException e) {throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Regex pattern for " + path + " failed trying to match a common file extension... (index out of bounds in the Matcher)");}
    }

}

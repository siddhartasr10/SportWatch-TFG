package com.reactive.SportWatch.routes;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import com.reactive.SportWatch.models.StreamingInfo;
import com.reactive.SportWatch.services.FetchService;
import com.reactive.SportWatch.services.JwtService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/")
public class FetchController {

    private final static Logger log = Logger.getLogger(FetchController.class.getName());

    private FetchService fetchService;
    private JwtService jwtService;

    @Autowired
    FetchController(FetchService fetchService, JwtService jwtService) {
        // this.streamService = streamService;
        this.fetchService = fetchService;
        this.jwtService = jwtService;
    }


    // fetch uploaded and non uploaded streams
    @GetMapping("streams")
    Flux <StreamingInfo> fetchAllStreams() {
        // By default optional duration is 2h.
        return fetchService.fetchUploadedStreams(Optional.empty())
                .concatWith(fetchService.fetchNonUploadedStreams(Optional.empty()));
    }

 /**
 * Fetches streaming information for the given stream ID.
 * This endpoint accepts a JSON request body containing optional settings such as how long the
 * stream URL should be valid (e.g., for signed URL generation).
 *
 * <p>The <code>urlDuration</code> field is optional and represents the validity duration (in hours)
 * of the returned stream URL. If omitted, a default duration may be applied by the backend.</p>
 *
 * <p>Expected JSON request body:
 * <pre>{@code <br>
 *
 * {
 *   "urlDuration": "2"  // (optional) Number of hours the stream URL should remain valid
 * }</pre>
 * </p>
 *
 * <p>Response Codes:</p>
 * <ul>
 *   <li><strong>200 OK</strong> — Successfully returns streaming info</li>
 *   <li><strong>400 Bad Request</strong> — If stream ID is less than 1 or if <code>urlDuration</code> is not a valid number</li>
 * </ul>
 *
 * @param stream_id the ID of the stream to fetch (must be > 0)
 * @param json a {@link Mono} containing a JSON map with optional fields like <code>urlDuration</code>
 * @return a {@link Mono} emitting {@link StreamingInfo} or an appropriate error
 */
    // Duration gets passed by hours as in the service.
    @PostMapping("stream/{stream_id}")
    Mono<StreamingInfo> fetchById(@PathVariable Integer stream_id, @RequestBody Mono<Map<String, String>> json) {
        if (stream_id < 1) return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "stream_id cannot be 0 bro you're kidding"));
        return json.flatMap(data -> {
                try {
                    Optional<Duration> urlDuration = Optional.ofNullable(Duration.ofHours(Integer.parseInt(data.get("urlDuration"))));
                    return fetchService.fetchStreamById(stream_id, urlDuration);
                } catch (NumberFormatException e) {
                    return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "urlDuration must be number of hours!"));
                }
            });
    }


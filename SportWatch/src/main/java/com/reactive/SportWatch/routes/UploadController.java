package com.reactive.SportWatch.routes;

import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import com.reactive.SportWatch.models.IvsChannelInfo;
import com.reactive.SportWatch.services.JwtService;
import com.reactive.SportWatch.services.UploadService;
import com.reactive.SportWatch.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * REST controller responsible for handling stream upload-related operations.
 * <p>
 * Provides endpoints for users to request streaming channels, either reusing existing
 * free ones or provisioning new ones if the limit hasn't been reached.
 * </p>
 *
 * <p>All endpoints in this controller are prefixed with <code>/api/</code>.</p>
 */
@RestController
@RequestMapping("api/")
public class UploadController {

    private final static Logger log = Logger.getLogger(UploadController.class.getName());

    /** Service for managing upload channels and stream provisioning. */
    private final UploadService uploadService;

    /** Service responsible for extracting and verifying JWT tokens. */
    private final JwtService jwtService;

    /** Service for accessing user account and ID information. */
    private final UserService userService;

    @Autowired
    UploadController(UploadService uploadService, JwtService jwtService, UserService userService) {
        // this.streamService = streamService;
        this.uploadService = uploadService;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    /**
     * Handles a request to find a free IVS (Interactive Video Service) streaming
     * channel for the authenticated user.
     * If no usable free channel is available, and the total number of channels is
     * below the configured limit,
     * a new channel is created instead.
     *
     * <p>
     * The request body must be a JSON object containing at least a non-null
     * <code>title</code> field.
     * Optional fields include <code>desc</code> and <code>category</code>.
     * </p>
     *
     * <p>
     * Important Notes:
     * <ul>
     * <li>Request body must be sent as <code>application/json</code>. Using
     * <code>@RequestBody Map</code> only works for JSON content type.</li>
     * <li>If the <code>title</code> field is missing or null, the server responds
     * with HTTP 400 (Bad Request).</li>
     * <li>If all existing channels are occupied and the maximum number of channels
     * has been reached, the server responds with HTTP 429 (Too Many Requests).</li>
     * </ul>
     * </p>
     *
     * <p>
     * Expected JSON request body structure:
     *
     * <pre>{@code
     * {
     *   "title": "My Live Stream",             // (required) Title of the channel
     *   "desc": "Optional description",        // (optional) Description of the stream
     *   "category": "Optional category name"   // (optional) Category label
     * }
     * }</pre>
     * </p>
     *
     * @param exch the {@link ServerWebExchange} used to extract cookies for JWT
     *             token lookup
     * @param json a {@link Mono} containing a JSON map with the channel metadata
     *             (title, desc, category) <br>
     * @return a {@link Mono} that emits an {@link IvsChannelInfo} representing the
     *         found or newly created channel,
     *         or emits an error with appropriate HTTP status (400, 401, 429) if
     *         validation fails or limits are reached
     */

    @PostMapping("request-channel")
    Mono<IvsChannelInfo> findFreeChannelOrNew(ServerWebExchange exch, @RequestBody Mono<Map<String, String>> json) {
        // I don't need to validate token, as filter already does, and it must exist.
        // All created tokens have subject so
        return json.filter(data -> data.get("title") != null)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "A title is required")))
            .flatMap(title -> jwtService.extractTokenFromCookies(exch.getRequest().getCookies()))
            .flatMap(token -> jwtService.getUsernameFromToken(token))
            .flatMap(username -> userService.findIdByUsername(username))
            .flatMap(userId -> Mono.zip(Mono.just(userId), json))
            .flatMap(tuple -> uploadService.findFreeChannel(tuple.getT1(), tuple.getT2().get("title"),
                                                            Optional.ofNullable(tuple.getT2().get("desc")), Optional.ofNullable(tuple.getT2().get("category")))
                     .flatMap(channelInfo -> {
                             if (channelInfo.streamKey() != null && channelInfo.rtmpsUrl() != null)
                                 return Mono.just(channelInfo);

                             return uploadService.countChannels().filter(count -> count < uploadService.MAXCHANNELS)
                                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                                            "Cannot have more than " + uploadService.MAXCHANNELS + " channels")))

                                    .flatMap(lessthanMax -> uploadService.newChannel(tuple.getT1(),
                                            tuple.getT2().get("title"),
                                            Optional.ofNullable(tuple.getT2().get("desc")),
                                            Optional.ofNullable(tuple.getT2().get("category")),
                                            Optional.empty()));
                         })
                     );
    }
   

}

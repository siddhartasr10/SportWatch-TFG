package com.reactive.SportWatch.routes;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.reactive.SportWatch.models.Comment;
import com.reactive.SportWatch.models.JsonResponse;
import com.reactive.SportWatch.services.CommentService;
import com.reactive.SportWatch.services.JwtService;
import com.reactive.SportWatch.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

// Not to confuse with FetchController to get the streams.
// This one is for stream comments and viewers.
// Dk if it will get used for notifications.
@RestController
@RequestMapping("api/")
public class CommentController {
    public static final Logger log = Logger.getLogger(CommentController.class.getName());

    private CommentService commentService;
    private UserService userService;
    private JwtService jwtService;

    @Autowired
    CommentController(CommentService commentService, JwtService jwtService, UserService userService) {
        this.commentService = commentService;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    // we don't need here neither author or commentid
    @PostMapping("comment")
    Mono<JsonResponse> createComment(@RequestBody Map<String, String> json, ServerWebExchange exch) {
        return extractComment(json)
            .flatMap(comment ->
                jwtService.extractTokenFromCookies(exch.getRequest().getCookies())
                    .flatMap(jwtService::getUsernameFromToken)
                    .doOnNext(username -> log.info("Comment before creating: " + comment))
                    .flatMap(userService::findIdByUsername)
                    .flatMap(id -> commentService.createComment(comment.authorId(id)))
                    .map(cmnt -> new JsonResponse("New Comment created for stream: " + cmnt.authorId().toString()))
            );
    }

    // Important, if i make payed streams comments of private streams have to check if the user is subscribed to that
    // stream author.
    @GetMapping("comment/{stream_id}")
    Mono<List<Comment>> listStreamComments(@PathVariable Integer stream_id, ServerWebExchange exch) {
        return commentService.findByStreamId(stream_id);

    }



    @PutMapping("comment/{comment_id}")
    Mono<JsonResponse> updateCommentMsg(@RequestBody String msg, @PathVariable Integer comment_id, ServerWebExchange exch) {
        return jwtService.extractTokenFromCookies(exch.getRequest().getCookies())
            .flatMap(token -> jwtService.getUsernameFromToken(token))
            .flatMap(username -> Mono.zip(userService.findIdByUsername(username), commentService.findAuthorById(comment_id)))
            .filter(tuple -> tuple.getT1() == tuple.getT2()) // Actual user's id must be equal to author's id.
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "You must be the owner of the comment you're updating!")))

            .flatMap(tuple -> commentService.updateCommentMsg(comment_id, msg))
            .map(cmnt -> new JsonResponse("Comment Updated with msg: " + msg));
    }

    @DeleteMapping("comment/{comment_id}")
    Mono<JsonResponse> deleteComment(@PathVariable Integer comment_id, ServerWebExchange exch) {
        return jwtService.extractTokenFromCookies(exch.getRequest().getCookies())
            .flatMap(token -> jwtService.getUsernameFromToken(token))
            .flatMap(username -> Mono.zip(userService.findIdByUsername(username), commentService.findAuthorById(comment_id)))
            .filter(tuple -> tuple.getT1() == tuple.getT2()) // Actual user's id must be equal to author's id.
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                                                                  "You must be the owner of the comment you're deleting!")))
            .flatMap(tuple -> commentService.deleteComment(comment_id))
            .filter(changes -> changes != 0)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "No deletions were made, maybe the commentId doesn't exist")))
            .map(changes -> new JsonResponse("Comment Deleted successfully"));
    }


    Mono<Comment> extractComment(Map<String, String> json) {
        return Mono.fromCallable(() -> {
            var comment = new Comment()
                         .streamId(Integer.parseInt(json.get("streamId")))
                         .comment(json.get("comment"));
            try {
                comment.authorId(Integer.parseInt(json.get("authorId")));
                log.info("Comment to be sent:" + comment);
                return comment;
            }
            catch (NumberFormatException err) {
                log.info("Comment to be sent:" + comment);
                return comment;
            }


            });
    }
}

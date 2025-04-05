package com.reactive.SportWatch.services;

import java.sql.Timestamp;
import java.util.logging.Logger;

import com.reactive.SportWatch.models.ExtUser;
import com.reactive.SportWatch.models.ExtUserDetails;
// import com.reactive.SportWatch.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;


@Service
public class UserService implements ReactiveUserDetailsService, ReactiveUserDetailsPasswordService {

    private static Logger logger = Logger.getLogger(UserService.class.toString());

    // private final JdbcTemplate jdbc; @Autowired public UserRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private final DatabaseClient dbClient;

    @Autowired
    public UserService(DatabaseClient databaseClient) {
        this.dbClient= databaseClient;
    }

    // Returnea todos los datos relevantes
    // Según el login que haga puedo acabar cambiando el mail a requerido pero por
    // el momento no lo es.
    public Mono<ExtUserDetails> extFindByUsername(String username) {
        Mono<ExtUserDetails> user = dbClient.sql("SELECT username, password, email, created_at, user_timezone, streamer_id FROM users WHERE username = :username")
            .bind("username", username).map((row, metadata) -> {
                    return ExtUser.extBuilder().username(row.get("username", String.class))
                        .password(row.get("password", String.class))
                        .email(row.get("email", String.class))
                        .created_at(row.get("created_at", Timestamp.class))
                        .timezone(row.get("user_timezone", Short.class))
                        .streamerId(row.get("streamer_id", Integer.class))
                        .authorities("USER") // La db actual no tiene roles, todos son users.
                        .build();

                    }).first();

        // returns an empty user if no user was found, the frontend can then validate that checking all 3 values.
        user.defaultIfEmpty(ExtUser.extBuilder().username("empty").password("empty").authorities("empty").build());

        return user;
    }
    // Returnea solo los datos necesarios para identificar al usuario
    public Mono<UserDetails> findByUsername(String username) {
        Mono<UserDetails> user = dbClient.sql("SELECT username, password FROM users WHERE username = :username").bind("username", username)
            .map((row, metadata) -> {
                    return User.builder().username(row.get("username", String.class))
                        .password(row.get("password", String.class))
                        .authorities("USER")
                        .build();
                }).first();

        // returns an empty user if no user was found, the frontend can then validate that checking all 3 values.
        user.defaultIfEmpty(User.builder().username("empty").password("empty").authorities("empty").build());

        return user;
    }

    public Mono<ExtUserDetails> findAllByUsername(String username) {
        Mono<ExtUserDetails> user = dbClient.sql("SELECT * FROM users WHERE username = :username")
            .bind("username", username).map((row, metadata) -> {
                    return ExtUser.extBuilder().username(row.get("username", String.class))
                        .password(row.get("password", String.class))
                        .email(row.get("email", String.class))
                        .created_at(row.get("created_at", Timestamp.class))
                        .timezone(row.get("user_timezone", Short.class))
                        .streamerId(row.get("streamer_id", Integer.class))
                        .follows(row.get("follows", int[].class))
                        .subscribed(row.get("subscribed", int[].class))
                        .notifications(row.get("notifications", char[][].class))
                        .authorities("USER") // La db actual no tiene roles, todos son users.
                        .build();

                    }).first();

        // returns an empty user if no user was found, the frontend can then validate that checking all 3 values.
        user.defaultIfEmpty(ExtUser.extBuilder().username("empty").password("empty").authorities("empty").build());

        return user;
    }


    public Mono<ExtUserDetails> updatePassword(ExtUserDetails user, String newPassword) {
        updatePassword((UserDetails) user, newPassword);
        // if doesn't throw, new password changed successfully
        return Mono.just(ExtUser.extWithUserDetails(user).password(newPassword).build());
    }

    public Mono<UserDetails> updatePassword(UserDetails user, String newPassword) {
        if (user.getPassword() == newPassword) return Mono.just(user);

        Mono<Long> changes = dbClient.sql("UPDATE users SET password = :password WHERE username = :username")
            .bind("username", user.getUsername())
            .bind("password", user.getPassword())
            .fetch().rowsUpdated();


        changes.doOnSuccess((res) -> {
                if (res == 0) {
                    if (findByUsername(user.getUsername()).block().getUsername() == "empty") logger.warning("User doesn't exist, this should never happen as only authenticated users can change its password");
                    logger.warning("No changes were made to the password, this should never happen");
                }});

        return Mono.just(user);
    }
//     private final UserRepository users;

//     @Autowired
//     UserService(UserRepository UserRepository) {
//         this.users = UserRepository;
//     }

//     public Mono<UserDetails> findByUsername(String username) {
//         return Mono.just(users.findByUsername(username));
//     }

//     public Mono<ExtUserDetails> extFindByUsername(String username) {
//         return Mono.just(users.extFindByUsername(username));
//     }

//     public Mono<UserDetails> updatePassword(UserDetails user, String password) {
//         return Mono.just(users.updatePassword(user, password));
//     }

//     public Mono<ExtUserDetails> updatePassword(ExtUserDetails user, String password) {
//         return Mono.just(users.updatePassword(user, password));
//     }

}

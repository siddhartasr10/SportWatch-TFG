package com.reactive.SportWatch.services;

import java.sql.Timestamp;
import java.util.Map;
import java.util.logging.Logger;

import com.reactive.SportWatch.models.ExtUser;
import com.reactive.SportWatch.models.ExtUserDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Mono;

// TODO: En un futuro hará falta crear métodos para añadir seguidores, suscriptores y notificaciones.
@Service
public class UserService implements ReactiveUserDetailsService, ReactiveUserDetailsPasswordService {

    private static Logger logger = Logger.getLogger(UserService.class.toString());

    private final DatabaseClient dbClient;

    private final PasswordEncoder encoder;

    @Autowired
    public UserService(DatabaseClient databaseClient, PasswordEncoder encoder) {
        this.dbClient = databaseClient;
        this.encoder = encoder;
    }

    // Returnea todos los datos relevantes
    // Según el login que haga puedo acabar cambiando el mail a requerido pero por
    // el momento no lo es.
    public Mono<ExtUserDetails> extFindByUsername(String username) {
        Mono<ExtUserDetails> user = dbClient.sql(
                "SELECT username, password, email, created_at, user_timezone, streamer_id FROM users WHERE username = :username")
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

        user.switchIfEmpty(Mono.error(new UsernameNotFoundException(String.format("Username: %s not found", username))));
        return user;
    }

    // Returnea solo los datos necesarios para identificar al usuario
    public Mono<UserDetails> findByUsername(String username) {
        Mono<UserDetails> user = dbClient.sql("SELECT username, password FROM users WHERE username = :username")
                .bind("username", username)
                .map((row, metadata) -> {
                    return User.builder().username(row.get("username", String.class))
                            .password(row.get("password", String.class))
                            .authorities("USER")
                            .build();
                }).first();

        user.switchIfEmpty(Mono.error(new UsernameNotFoundException(String.format("Username: %s not found", username))));

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

        user.switchIfEmpty(Mono.error(new UsernameNotFoundException(String.format("Username: %s not found", username))));

        return user;
    }

    public Mono<Boolean> isUsernameTaken(String username) {
        return dbClient.sql("SELECT username FROM users WHERE username = :username")
                .bind("username", username)
                .fetch().first().switchIfEmpty(Mono.just(Map.of()))
                .map(userMap -> !userMap.isEmpty());
    }

    public Mono<ExtUserDetails> updatePassword(ExtUserDetails user, String newPassword) {
        updatePassword((UserDetails) user, newPassword);
        // if doesn't throw, new password changed successfully
        return Mono.just(ExtUser.extWithUserDetails(user).password(newPassword).build());
    }

    public Mono<UserDetails> updatePassword(UserDetails user, String newPassword) {
        if (user.getPassword() == newPassword)
            return Mono.just(user);

        Mono<Long> changes = dbClient.sql("UPDATE users SET password = :password WHERE username = :username")
                .bind("username", user.getUsername())
                .bind("password", user.getPassword())
                .fetch().rowsUpdated();

        changes.doOnSuccess((res) -> {
            if (res == 0) {
                findByUsername(user.getUsername())
                        .doOnNext(u -> {

                                if (u.getUsername() == "empty" && u.getPassword() == "empty" || u.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_empty")))
                                    logger.warning("User doesn't exist, this should never happen as only authenticated users can change its password");

                                logger.warning("No changes were made to the password but user exists, this should never happen, except same password its being set");
                        });
            }
        });

        return Mono.just(user);
    }
    public Mono<Short> updateTimezone(String username, short timezone) {
        dbClient.sql("UPDATE users SET user_timezone = :timezone WHERE username = :username")
                .bind("username", username)
                .bind("timezone", timezone)
                .fetch().rowsUpdated()
                .doOnNext(changes -> {
                        if (changes == 0) logger.warning("No timezone was changed");
                        logger.info("Timezone changed successfully");
                });

        return Mono.just(timezone);
    }

    public Mono<UserDetails> createUser(UserDetails user) {
        return isUsernameTaken(user.getUsername())
                .flatMap(isTaken -> {
                    logger.info(isTaken.toString());
                    if (isTaken)
                        return Mono.error(new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Username already taken"));

                    return dbClient.sql("INSERT INTO users (username, password) VALUES (:username, :password)")
                            .bind("username", user.getUsername())
                            .bind("password", encoder.encode(user.getPassword()))
                            .fetch().rowsUpdated()
                            .doOnNext(changes -> {
                                if (changes == 0) logger.warning("No user was created");
                                logger.info("User created successfully");
                            })
                            .flatMap(v -> {logger.info("User created."); return Mono.just(user);});
                });

    }


    public Mono<ExtUserDetails> createUser(ExtUserDetails user)  {
        return isUsernameTaken(user.getUsername())
                .flatMap(isTaken -> {
                    if (isTaken)
                        return Mono.error(new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Username already taken"));

                    StringBuilder columns = new StringBuilder("username, password, user_timezone");
                    StringBuilder values = new StringBuilder(":username, :password, :timezone");

                    // Null check has to be made to be perfectly sure im not messing up.
                    if (user.getEmail() != null) {
                        columns.append(", email");
                        values.append(", :email");
                    }

                    String sql = String.format("INSERT INTO users (%s) VALUES (%s)", columns, values);

                    GenericExecuteSpec pausedSpec = dbClient.sql(sql)
                            .bind("username", user.getUsername())
                            .bind("password", encoder.encode(user.getPassword()))
                            .bind("timezone", user.getTimezone());

                    // Have to reassign because object is inmmutable, so a new copy is created.
                    if (user.getEmail() != null) pausedSpec = pausedSpec.bind("email", user.getEmail());

                    return pausedSpec.fetch()
                            .rowsUpdated()
                            .doOnNext(changes -> {
                                    if (changes == 0)
                                        logger.warning("No user was created");
                                    logger.info("User created successfully");
                            })
                            .flatMap(v -> Mono.just(user));
                });
    }

    public Mono<Void> deleteUser(String username) {
        return dbClient.sql("DELETE FROM users WHERE username = :username")
                .bind("username", username)
                .fetch() .rowsUpdated()
                .flatMap(changes -> {

                    switch(changes.intValue()) {
                        case 0 -> logger.warning("No user was deleted");
                        case 1 -> logger.info("User deleted succesfully");}

                    if (changes > 1)
                        logger.warning("More than one row affected, something bad happened...");

                    return Mono.empty();
                });

    }

    /**
     * DEBUG FUNCTION DON'T USE AT PROD
     *
     * Allows me to query anything to the db for debug purposes.
     * I use it in tests, and probably is the only place where this correct to use.
     *
     */
    public void query(String sql) {
        dbClient.sql(sql).fetch().rowsUpdated().subscribe();
    }



}

package com.reactive.SportWatch.repositories;

import java.util.logging.Logger;

import com.reactive.SportWatch.models.ExtUser;
import com.reactive.SportWatch.models.ExtUserDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

// UserDetailsService will implement reactiveUserService and reactiveUserDetailspasswordService
// So it needs "findByUsername" and "updatePassword" methods to meet contract requirements
// Future TODO: add specific select when needed
@Repository
public class UserRepository {

    private final JdbcTemplate jdbc;

    private static Logger logger = Logger.getLogger(UserRepository.class.toString());

    @Autowired
    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Returnea todos los datos relevantes y que requiere para extUser (streamer Id e email no lo requiere, pueden ser null)
    // Según el login que haga puedo acabar cambiando el mail a requerido pero por el momento no lo es.
    public ExtUserDetails extFindByUsername(String username) throws UsernameNotFoundException {
        ExtUserDetails user = jdbc.query("SELECT username, password, email, created_at, user_timezone, streamer_id FROM Users WHERE username = ?", (rs) -> {
            try {
                if (rs.next()) {
                    ExtUserDetails row = ExtUser.extBuilder().username(rs.getString("username"))
                            .password(rs.getString("password"))
                            .email(rs.getString("email"))
                            .created_at(rs.getTimestamp("created_at"))
                            .timezone(rs.getShort("user_timezone"))
                            .streamerId(rs.getInt("streamer_id"))
                            .authorities("USER") // La db actual no tiene roles, todos son users.
                            .build();

                    return row;
                }

                throw new UsernameNotFoundException(String.format("Username: %s wasn't found", username));

            } catch (DataAccessException e) {
                logger.warning(String.format("An error occured trying to return an user by username: %s",
                        (Object[]) e.getStackTrace()));
                return null;
            }
        }, username);

        return user;
    }

    public ExtUserDetails updatePassword(ExtUserDetails user, String newPassword) throws UsernameNotFoundException {
        updatePassword((UserDetails) user, newPassword);

        return ExtUser.extWithUserDetails(user)
                .password(newPassword)
                .build();
    }

    // Returnea solo los datos necesarios para identificar al usuario
    public UserDetails findByUsername(String username) {
        UserDetails user = jdbc.query("SELECT username, password FROM Users WHERE username = ?", (rs) -> {
            try {
                if (rs.next()) {
                    UserDetails row = User.builder().username(rs.getString("username"))
                            .password(rs.getString("password"))
                            .authorities("USER")
                            .build();

                    return row;
                }

                throw new UsernameNotFoundException(String.format("Username: %s wasn't found", username));

            } catch (DataAccessException e) {
                logger.warning(String.format("An error occured trying to return an user by username: %s",
                        (Object[]) e.getStackTrace()));
                return null;
            }
        }, username);

        return user;
    }


    public UserDetails updatePassword(UserDetails user, String newPassword) throws UsernameNotFoundException {
        try {
            int changes = jdbc.update("UPDATE Users SET password = ? WHERE username = ?", user.getPassword(),
                    user.getUsername());
            // If no change was made we check if user exists, if it doesn't throw an error
            // then logger will warn.
            if (changes == 0) {
                findByUsername(user.getUsername());
                logger.warning("Password update didn't change anything, is the password being changed the same?");
                return user;
            }

        } catch (DataAccessException e) {
            logger.warning(
                    String.format("An error occured trying to update a password: %s", (Object[]) e.getStackTrace()));
            return null;
        }

        return User.withUserDetails(user)
                .password(newPassword)
                .build();
    }

    public ExtUserDetails findAllByUsername(String username) {
        ExtUserDetails user = jdbc.query("SELECT * FROM Users WHERE username = ?", (rs) -> {
            try {
                if (rs.next()) {
                    ExtUserDetails row = ExtUser.extBuilder().username(rs.getString("username"))
                            .password(rs.getString("password"))
                            .email(rs.getString("email"))
                            .created_at(rs.getTimestamp("created_at"))
                            .timezone(rs.getShort("user_timezone"))
                            .streamerId(rs.getInt("streamer_id"))
                            .authorities("USER") // La db actual no tiene roles, todos son users.
                            // Idk if this is going to work
                            .follows((int[]) (Object) rs.getArray("follows"))
                            .subscribed((int[]) (Object) rs.getArray("subscribed"))
                            .notifications((char[][]) (Object) rs.getArray("notifications"))
                            .build();

                    return row;
                }

                throw new UsernameNotFoundException(String.format("Username: %s wasn't found", username));

            } catch (DataAccessException e) {
                logger.warning(String.format("An error occured trying to return an user by username: %s",
                        (Object[]) e.getStackTrace()));
                return null;
            }
        }, username);

        return user;
    }
}

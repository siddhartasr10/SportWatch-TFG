package com.reactive.SportWatch.models;

import java.util.Collection;
import java.util.logging.Logger;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

// Info: El encoding de la contraseña se hace aparte del builder por cuestiones de seguridad
// TODO: termina de sobreescribir los with* y checkea que esté todo listo
public class ExtUser extends User {
    // I use a different logger than the super class
    private static final Logger logger = Logger.getLogger(ExtUser.class.toString());
	private final String email;
    // Not mentioned params in the constructor: all (set to true in the shortened super constructor)
    /**
	 * @param enabled set to <code>true</code> if the user is enabled
	 * @param accountNonExpired set to <code>true</code> if the account has not expired
	 * @param credentialsNonExpired set to <code>true</code> if the credentials have not expired
	 * @param accountNonLocked set to <code>true</code> if the account is not locked
	 */
    public ExtUser(String username, String password, String email, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.email = email;
    }

    // Constructor to allow builder to bypass internal builder's encoded password
    public ExtUser(User user, String password, String email) {
        super(user.getUsername(), password, user.getAuthorities());
        this.email = email;

    }

    public String getEmail() {
		return email;
	}

    /* Copia de UserBuilder de User pero con email y más simple (cutre)
    * Usa un UserBuilder de User y cambia el build para crear con el user buildeado
    * Así los métodos withUsername heredados de User pueden ser usados con este nuevo
    * UserBuilder
    */
    public static final class UserBuilder {

        private String email;

        /* Little trick to avoid using encoded password, as its a deprecated procedure
         * and is marked as insecure, so encoding will be carried over outside the builder
         * to do that using a internal User.UserBuilder I need to store the password twice
         * and create the ExtUser with the tmp one. (I have to add the password twice cause if I don't add it to the internalBuilder a null error will throw)
         * read the doc on "User withDefaultPasswordEncoder" where it says that its better
         * to build the password already encoded (which with the default UserBuilder is impossible)
         * because even if you pass it to UserBuilder.password() encoded, it will get encoded again
         * which is another issue the doc mentions.
         */
        private String tmpPassword;

        private final User.UserBuilder internalBuilder = User.builder();

        // Non used fields, my user won't be needing this rn, maybe in the future.
		// private boolean accountExpired; private boolean accountLocked; private boolean credentialsExpired; private boolean disabled;

		/**
		 * Creates a new instance
		 */
		private UserBuilder() {}


		/**
		 * Populates the username. This attribute is required.
		 * @param username the username. Cannot be null.
		 * @return the {@link UserBuilder} for method chaining (i.e. to populate
		 * additional attributes for this user)
		 */
		public UserBuilder username(String username) {
            this.internalBuilder.username(username);
			return this;
		}

		/**
		 * Populates the password. This attribute is required.
		 * @param password the password. Cannot be null.
		 * @return the {@link UserBuilder} for method chaining (i.e. to populate
		 * additional attributes for this user)
		 * Does a little trick to avoid the encoded password
		 * so its externalized for the user to encode it as withDefaultPasswordEncoder docs says
		 * its better. password is added to internalBuilder to avoid null errors.
		 */
		public UserBuilder password(String password) {
            this.tmpPassword = password;
            this.internalBuilder.password(password);
			return this;
		}


		/**
		 * Populates the email. This attribute is required.
		 * @param email the email. Cannot be null.
		 * @return the {@link UserBuilder} for method chaining (i.e. to populate
		 * additional attributes for this user)
		 */
		public UserBuilder email(String email) {
			Assert.notNull(email, "email cannot be null");
			this.email = email;
			return this;
		}

		/**
		 * Populates the roles. This method is a shortcut for calling
		 * {@link #authorities(String...)}, but automatically prefixes each entry with
		 * "ROLE_". This means the following:
		 *
		 * <code>
		 *     builder.roles("USER","ADMIN");
		 * </code>
		 *
		 * is equivalent to
		 *
		 * <code>
		 *     builder.authorities("ROLE_USER","ROLE_ADMIN");
		 * </code>
		 *
		 * <p>
		 * This attribute is required, but can also be populated with
		 * {@link #authorities(String...)}.
		 * </p>
		 * @param roles the roles for this user (i.e. USER, ADMIN, etc). Cannot be null,
		 * contain null values or start with "ROLE_"
		 * @return the {@link UserBuilder} for method chaining (i.e. to populate
		 * additional attributes for this user)
		 */
		public UserBuilder roles(String... roles) {
            this.internalBuilder.roles(roles);
            return this;
		}

		/**
		 * Populates the authorities. This attribute is required.
		 * @param authorities the authorities for this user. Cannot be null, or contain
		 * null values
		 * @return the {@link UserBuilder} for method chaining (i.e. to populate
		 * additional attributes for this user)
		 * @see #roles(String...)
		 */
		public UserBuilder authorities(GrantedAuthority... authorities) {
            this.internalBuilder.authorities(authorities);
			return this;
		}

		/**
		 * Populates the authorities. This attribute is required.
		 * @param authorities the authorities for this user. Cannot be null, or contain
		 * null values
		 * @return the {@link UserBuilder} for method chaining (i.e. to populate
		 * additional attributes for this user)
		 * @see #roles(String...)
		 */
		public UserBuilder authorities(Collection<? extends GrantedAuthority> authorities) {
            this.internalBuilder.authorities(authorities);
			return this;
		}

		/**
		 * Populates the authorities. This attribute is required.
		 * @param authorities the authorities for this user (i.e. ROLE_USER, ROLE_ADMIN,
		 * etc). Cannot be null, or contain null values
		 * @return the {@link UserBuilder} for method chaining (i.e. to populate
		 * additional attributes for this user)
		 * @see #roles(String...)
		 */
		public UserBuilder authorities(String... authorities) {
            this.internalBuilder.authorities(authorities);
            return this;
		}

        // Little trick to avoid password encoding being carried over by the builder, which is marked as deprecated
        // and as insecure, so we don't use internalBuilder's created user's password
        // as its encoded and encoding in ExtUser is external not internal.
		public UserDetails build() {
            User internalUser = (User) internalBuilder.build();
			return new ExtUser(internalUser, tmpPassword, this.email);
		}

	}

    // This function isn't implemented by superclass "User", but is useful for my UserBuilder impl
    // public static User.UserBuilder withPassword(String password) {
    //     User.with
    // }

    // public static User.UserBuilder builder() {
    //     logger.warning("ExtUser builder is extBuilder(), can't return superclass builder, I will return it anyways bc return type can't change, but with invalid info");
    //     return User.withUsername("fooBar");
    // }

    public static User.UserBuilder withDefaultPasswordEncoder() {
        logger.warning("ExtUser Doesn't allow this, encode the password separately, I will be returning empty UserBuilder");
        return User.builder();
    }

    public static User.UserBuilder withUsername(String username) {
        logger.warning("ExtUser doesn't use User.UserBuilder, empty invalid builder will be returned instead");
        return User.builder();
    }

    public static User.UserBuilder builder() {
        logger.warning("ExtUser doesn't use builder(), use extBuilder instead!");
        return User.builder();
    }

    public static UserBuilder extBuilder() {
       return new UserBuilder();
    }
}

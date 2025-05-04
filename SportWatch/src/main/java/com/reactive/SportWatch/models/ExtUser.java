package com.reactive.SportWatch.models;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

// Info: El encoding de la contraseña se hace aparte del builder por cuestiones de seguridad
public class ExtUser extends User implements ExtUserDetails {
    // I use a different logger than the super class
    private static final Logger logger = Logger.getLogger(ExtUser.class.toString());
	private final String email;
    private final Timestamp created_at;
    private final int streamerId;
    private final List<Integer> follows;
    private final List<Integer> subscribed;
    /*
     * @Tparam char[l] l=128 is what I setted on the db
     * some logic will have to be made to check the array until "\0" (null terminator)
     * */
    private final List<char[]> notifications;
    // Not mentioned params in the constructor: all (set to true in the shortened super constructor)
    /**
	 * @param enabled set to <code>true</code> if the user is enabled
	 * @param accountNonExpired set to <code>true</code> if the account has not expired
	 * @param credentialsNonExpired set to <code>true</code> if the credentials have not expired
	 * @param accountNonLocked set to <code>true</code> if the account is not locked
	 */
     // If built from the constructor it will only check null on username and password (this is intended, as only debug extUsers will be created using the constructor)
    public ExtUser(String username, String password, String email, Collection<? extends GrantedAuthority> authorities, Timestamp created_at, int streamerId, List<Integer> follows, List<Integer> subscribed, List<char[]> notifications) {
        super(username, password, authorities);
        this.email = email;
        this.created_at = created_at;
        this.streamerId = streamerId;
        this.follows = follows;
        this.subscribed = subscribed;
        this.notifications = notifications;
    }

    // Constructor to allow builder to bypass internal builder's encoded password
    public ExtUser(User user, String password, String email, Timestamp created_at, int streamerId, List<Integer> follows, List<Integer> subscribed, List<char[]> notifications) {
        super(user.getUsername(), password, user.getAuthorities());
        this.email = email;
        this.created_at = created_at;
        this.streamerId = streamerId;
        this.follows = follows;
        this.subscribed = subscribed;
        this.notifications = notifications;
    }

    public String getEmail() {
		return email;
	}

    public Timestamp getCreated_at() {
        return created_at;
    }

    public int getStreamerId() {
        return streamerId;
    }

    public List<Integer> getFollows() {
        return follows;
    }

    public List<Integer> getSubscribed() {
        return subscribed;
    }

    public List<char[]> getNotifications() {
        return notifications;
    }

    @Override
    public String toString() {
        return  String.format("ExtUser: " +
                              "<Username: %s, Password: %s, "
                              + "Email: %s, authorities: %s, "
                              + "created at: %s "
                              + "streamerId: %s, follows: %s, "
                              + "subscribed to: %s, with %s notifications>",
                              this.getUsername(), this.getPassword(),
                              this.getEmail(), this.getAuthorities(),
                              this.getCreated_at(),
                              this.getStreamerId(), this.getFollows(),
                              this.getSubscribed(), this.getNotifications());
    }

    /* Copia de UserBuilder de User pero con email y más simple (cutre)
    * Usa un UserBuilder de User y cambia el build para crear con el user buildeado
    * Así los métodos withUsername heredados de User pueden ser usados con este nuevo * UserBuilder
    */
    public static final class UserBuilder {

        private String email;

        private Timestamp created_at;

        private int streamerId;

        private List<Integer> follows;
        private List<Integer> subscribed;

        /*
        * @Tparam char[l] l=128 is what I setted on the db
        * */
        private List<char[]> notifications;
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
		 * Populates the email.
		 * @param email the email. Can be null.
		 * @return the {@link UserBuilder} for method chaining (i.e. to populate
		 * additional attributes for this user)
		 */
		public UserBuilder email(String email) {
			// Assert.notNull(email, "email cannot be null");
			this.email = email;
			return this;
		}

		/**
		 * Populates when the user was created
		 * @param created_at can't be null
		 * @return the {@link UserBuilder} for method chaining (i.e. to populate
		 * additional attributes for this user)
		 * It shouldn't be set at register time postgres does it for you.
		 */
        public UserBuilder created_at(Timestamp created_at) {
			Assert.notNull(created_at, "created_at cannot be null");
			this.created_at = created_at;
			return this;
        }

		/**
		 * Populates the Streamer ID
		 * @param streamerId can be null
		 * @return the {@link UserBuilder} for method chaining (i.e. to populate
		 * It shouldn't be set at register time
		 * additional attributes for this user)
		 */
        public UserBuilder streamerId(int streamerId) {
			// streamerId can be null
			// Assert.notNull(streamerId, "... cannot be null");
			this.streamerId = streamerId;
			return this;
        }

        /**
         * Populates the follows List.
         * @param follows the StreamerIds of the streamers you follow
         * it can be null and shouldn't be set at register time.
         *  * */
        public UserBuilder follows(List<Integer> follows) {
            this.follows = follows;
            return this;
        }

        public UserBuilder follows(int[] follows) {
            List<Integer> list = new ArrayList<Integer>();
            for (int i = 0; i < follows.length; i++) list.add(follows[i]);
            return follows(list);
        }


        /**
         * @param subscribed the StreamerIds of the streamers you're a member of
         * it can be null.
         *  * */
        public UserBuilder subscribed(List<Integer> subscribed) {
            this.subscribed = subscribed;
            return this;
        }

        public UserBuilder subscribed(int[] subscribed) {
            List<Integer> list = new ArrayList<Integer>();
            for (int i = 0; i < subscribed.length; i++) list.add(subscribed[i]);
            return subscribed(list);
        }

        /**
         * @param notifications the text of the notifications you've got
         * char array is char[128]
         * it can be null.
         *  * */
        public UserBuilder notifications(List<char[]> notifications) {
            for (int i = 0; i < notifications.size(); i++) Assert.isTrue(notifications.get(i).length == 128, "Invalid length of notification char, has to be 128");
            this.notifications = notifications;
            return this;
        }

        // Won't create a list and then call its overloaded method
        // As it would iterate two times per notification list.
        public UserBuilder notifications(char[][] notifications) {
            List<char[]> list = new ArrayList<char[]>();
            for (int i = 0; i < notifications.length; i++) {
                Assert.isTrue(notifications[i].length == 128, "Invalid length of notification char, has to be 128");
                list.add(notifications[i]);
            }

            this.notifications = list;
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
		public ExtUserDetails build() {
            User internalUser = (User) this.internalBuilder.build();
			return new ExtUser(internalUser, this.tmpPassword, this.email, this.created_at, this.streamerId, this.follows, this.subscribed, this.notifications);
		}

	}

    @SuppressWarnings("deprecation")
    public static User.UserBuilder withDefaultPasswordEncoder() {
        logger.warning("ExtUser Doesn't allow this, encode the password separately, I will be returning empty UserBuilder");
        return User.builder();
    }

    public static User.UserBuilder withUsername(String username) {
        logger.warning("ExtUser doesn't use User.UserBuilder, empty invalid builder will be returned");
        logger.warning("Use extWithUsername instead");
        return User.builder();
    }

    public static User.UserBuilder builder() {
        logger.warning("ExtUser doesn't use builder(), use extBuilder instead!");
        logger.warning("Use extBuilder instead");
        return User.builder();
    }

    public static User.UserBuilder withUserDetails(UserDetails userDetails) {
        logger.warning("ExtUser doesn't use User.UserBuilder, empty invalid builder will be returned");
        logger.warning("Use extWithUserdetails instead");
        return User.builder();
    }

    public static UserBuilder extWithEmail(String email) {
        return extBuilder().email(email);
    }

    public static UserBuilder extWithUsername(String username) {
        return extBuilder().username(username);
    }
    //
    public static UserBuilder extBuilder() {
       return new UserBuilder();
    }
    // I don't use account Expired neither Locked, or disabled or nonexpired credentials
    public static UserBuilder extWithUserDetails(ExtUserDetails extendedUserDetails) {
        return extBuilder().username(extendedUserDetails.getUsername())
            .password(extendedUserDetails.getPassword())
            .authorities(extendedUserDetails.getAuthorities())
            .email(extendedUserDetails.getEmail())
            .created_at(extendedUserDetails.getCreated_at())
            .streamerId(extendedUserDetails.getStreamerId());

    }

}

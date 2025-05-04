package com.reactive.StreamServer;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

// import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.logging.Logger;

import com.reactive.SportWatch.SportWatchApp;
import com.reactive.SportWatch.services.JwtService;
import com.reactive.SportWatch.services.UserService;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
// SpringExtension.class @ExtendWith(MockitoExtension.class) @ContextConfiguration(classes = SportWatchApp.class) @Import({UserService.class, SecurityConfig.class, WebTestClient.class, DatabaseClient.class})
// @ExtendWith(MockitoExtension.class)
// TODO: escribe tests para un login válido y uno para un registro (hazlo una vez y dejalo comentado).
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(classes = SportWatchApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
@AutoConfigureWebTestClient
public class AuthTests {
    // @Autowired private ReactiveAuthenticationManager authManager;
    // @LocalServerPort private String port;
    @Autowired
    private WebTestClient webClient;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    // @Autowired
    // private BCryptPasswordEncoder encoder;

    private final String XSRFTOKEN = UUID.randomUUID().toString();

    private final String TEST_USERNAME = "juanitoreal";
    private final String TEST_PASSWORD = "juanito";

    private final static Logger logger = Logger.getLogger(AuthTests.class.getName());

    /**
     * This method creates a user in the db
     * normally it will be commented out to avoid
     * increasing the id sequence number.
     */
    @Test
    @Order(1)
    protected void successfulRegister() {
        logger.info("succesfulRegister Test starting...");
        this.webClient.post().uri("/api/register")
            .cookie("XSRF-TOKEN", XSRFTOKEN)
            .header("X-XSRF-TOKEN", XSRFTOKEN)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue("username="+this.TEST_USERNAME+"&password="+this.TEST_PASSWORD)
            .exchange()
            .expectStatus().isAccepted()
            .expectCookie().httpOnly("authToken", true)
            .expectCookie().httpOnly("user", false);

        logger.info("succesfulRegister Test executed");
    }

    @Test
    protected void successfulLogin() {
        logger.info("succesfulLogin Test starting...");
        this.webClient.post().uri("/api/login")
            .cookie("XSRF-TOKEN", XSRFTOKEN)
            .header("X-XSRF-TOKEN", XSRFTOKEN)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue("username="+this.TEST_USERNAME+"&password="+this.TEST_PASSWORD)
            .exchange()
            .expectStatus().isAccepted()
            .expectCookie().httpOnly("authToken", true)
            .expectCookie().httpOnly("user", false);

        logger.info("succesfulLoginTest Test executed");
    }

    /**
     * Makes sure that a user that registers with the same data
     * of another already registered user gets detected by the
     * server and throws some kind of error.
     *  */
    @Test
    protected void registerUserAlreadyExists() {
        logger.info("registerUserAlreadyExists Test starting...");
        this.webClient.post().uri("/api/register")
            .cookie("XSRF-TOKEN", XSRFTOKEN)
            .header("X-XSRF-TOKEN", XSRFTOKEN)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue("username="+this.TEST_USERNAME+"&password="+this.TEST_PASSWORD)
            .exchange()
            .expectStatus().value(status -> assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), status, "Status has to be Non acceptable!"))
            .expectCookie().doesNotExist("authToken")
            .expectCookie().doesNotExist("user");

        logger.info("registerUserAlreadyExists Test executed");
    }

    /**
     * Test that either login or register post requests will throw
     * if no csrf token is added to the cookies and headers.
     * (Double Submit naive impl, check owasp cheatsheet)
     *  */
    @Test
    protected void loginAndRegisterRequireCsrf() {
        logger.info("loginAndRegisterRequireCsrf Test starting...");
        this.webClient.post().uri("/api/login")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .bodyValue("username="+this.TEST_USERNAME+"&password="+this.TEST_PASSWORD)
        .exchange()
        .expectStatus().isUnauthorized();


        this.webClient.post().uri("/api/register")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .bodyValue("username="+"newUser"+"&password="+"newUser")
        .exchange()
        .expectStatus().isUnauthorized();




        logger.info("loginAndRegisterRequireCsrf Test executed");
    }

    // This test will change adding valid authToken cookie when logoutController
    // Implements a blocklist for logged out JWTs

    @Test
    protected void successfulLogout() {
        logger.info("successfulLogout Test starting...");
        String token = jwtService.generateToken(TEST_USERNAME).block();

        this.webClient.post().uri("/api/logout")
        .cookie("user", "juanito")
        .cookie("authToken", token)
        .cookie("XSRF-TOKEN", XSRFTOKEN)
        .header("X-XSRF-TOKEN", XSRFTOKEN)
        .exchange()
        .expectStatus().isOk()
        .expectCookie().maxAge("user", Duration.ZERO)
        .expectCookie().valueEquals("user", "")
        .expectCookie().maxAge("authToken", Duration.ZERO)
        .expectCookie().valueEquals("authToken", "");


        logger.info("successfulLogout Test executed");

    }



    // TODO: change this method to incorporate a /api/deleteAccount route.
    @Order(Integer.MAX_VALUE)
    @Test
    protected void deleteAccountSuccesfully() {
        logger.info("deleteAccountSuccesfully Test starting...");
        userService.deleteUser(TEST_USERNAME).block();
        //  FIXME: THIS IS DANGEROUS, SEQUENCE WILL START AS 1, IF ONE USER EXISTS IDs WILL CONFLICT
        //  replace with method before and create and enpoint ffs
        userService.query("ALTER SEQUENCE users_user_id_seq restart");
        // userService.query("SELECT setval('users_id_seq', (SELECT MAX(id) FROM users))");
        logger.info("deleteAccountSuccesfully Test executed");
    }

}

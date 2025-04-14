package com.reactive.SportWatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration; @SpringBootApplication(exclude = {ReactiveUserDetailsServiceAutoConfiguration.class}) // no es necesario y en tu caso ni va porque el problema es que usas jdbc starter seguramente r2dbc starter es el tuyo jajaj

@SpringBootApplication
public class SportWatchApp {

	public static void main(String[] args) {
		SpringApplication.run(SportWatchApp.class, args);
		// System.out.println(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("juanito"));

	}
}

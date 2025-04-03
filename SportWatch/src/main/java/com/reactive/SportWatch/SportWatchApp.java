package com.reactive.SportWatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication(exclude = {SecurityAutoConfiguration.class}) No es necesario desactivarla se puede ir sobreescribiendo
@SpringBootApplication
public class SportWatchApp {

	public static void main(String[] args) {
		SpringApplication.run(SportWatchApp.class, args);
	}

}

package com.conti;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ContiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ContiApplication.class, args);
	}

}

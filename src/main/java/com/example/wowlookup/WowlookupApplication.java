package com.example.wowlookup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WowlookupApplication {

	public static void main(String[] args) {
		System.out.println("Sebbe startar");
		SpringApplication.run(WowlookupApplication.class, args);

		// BlizzardTokenGenerator tokenGenerator = new BlizzardTokenGenerator();
		// tokenGenerator.getToken();
	}

}

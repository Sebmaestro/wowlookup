package com.example.wowlookup.service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.JsonNode;

@Service
public class BlizzardTokenGenerator {

    private final String clientId;
    private final String clientSecret;
    private final RestClient restClient;
    private String token;
    private Instant tokenExpiry = Instant.EPOCH; // Initialize to a past time

    public BlizzardTokenGenerator(
        @Value("${blizzard.client-id}") String clientId,
        @Value("${blizzard.client-secret}") String clientSecret) {
    
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.restClient = RestClient.builder()
            .baseUrl("https://oauth.battle.net")
            .build();
    }

    public synchronized String getToken() {
        if (token != null && Instant.now().isBefore(tokenExpiry)) {
            return token; // Return cached token if valid
        }

        JsonNode response = restClient.post()
                .uri("/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .headers(h->h.setBasicAuth(clientId, clientSecret))
                .body("grant_type=client_credentials")
                .retrieve().body(JsonNode.class);

                System.out.println("this is the shit\n"+response);
                token = response.path("access_token").asString();
                long expiresIn = response.path("expires_in").asLong(0);
                tokenExpiry = Instant.now().plusSeconds(expiresIn -60); // Refresh 1 minute before expiry

        return token;
    }
}

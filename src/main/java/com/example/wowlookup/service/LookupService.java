package com.example.wowlookup.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.JsonNode;

@Service
public class LookupService {

    private final BlizzardTokenGenerator tokenGenerator;
    private final RestClient restClient;

    public LookupService(BlizzardTokenGenerator tokenGenerator) {
        this.tokenGenerator = tokenGenerator;
        this.restClient = RestClient.create();
    }

    /**
     * Fetches the Mythic+ score of a character for the current season.
     * @param characterName
     * @param realm
     * @return M+ score as a String, "noScoreFound" if char exists but no score, or "noChar" if character doesn't exist
     */
    public String getCharacterMplusScore(String characterName, String realm) {
        try {
            JsonNode response = callBlizzardApi(
                "https://eu.api.blizzard.com/profile/wow/character/%s/%s/mythic-keystone-profile?namespace=profile-eu&locale=en_US",
                realm.toLowerCase(),
                characterName.toLowerCase()
            );

            // Check if current_mythic_rating exists
            if (response.path("current_mythic_rating").isMissingNode()) {
                return "noScoreFound"; // Character exists but no M+ profile data
            }

            // Check if character is unranked (alpha channel = 0) or has no best_runs
            double alpha = response.path("current_mythic_rating").path("color").path("a").asDouble(1.0);
            if (alpha == 0 || response.path("current_period").path("best_runs").size() == 0) {
                return "noScoreFound"; // Character exists but no score in current season
            }

            String score = response.path("current_mythic_rating").path("rating").asString();
            return score;
        } catch (HttpClientErrorException.NotFound e) {
            return "noChar"; // Character or realm not found (404)
        }
    }

    /**
     * Fetches the primary professions of a character.
     * @param characterName
     * @param realm
     * @return List of profession names, or an empty list if none found or character doesn't exist
     */
    public List<String> getCharacterProfessions(String characterName, String realm) {
        try {
            JsonNode response = callBlizzardApi(
                "https://eu.api.blizzard.com/profile/wow/character/%s/%s/professions?namespace=profile-eu&locale=en_EU",
                realm.toLowerCase(),
                characterName.toLowerCase()
            );

            List<String> professions = new ArrayList<>();
            for (JsonNode primary : response.path("primaries")) {
                String professionName = primary.path("profession").path("name").asString("");
                if (!professionName.isEmpty()) {
                    professions.add(professionName);
                }
            }

            return professions;
        } catch (HttpClientErrorException.NotFound e) {
            return new ArrayList<>(); // Character or realm not found (404)
        }
    }



    /**
     * Helper method to call Blizzard's API with authentication and return the response as a JsonNode.
     * @param urlTemplate
     * @param args
     * @return API response as a JsonNode
     */
    private JsonNode callBlizzardApi(String urlTemplate, Object... args) {
        String url = String.format(urlTemplate, args);
        String token = tokenGenerator.getToken();

        return restClient.get()
            .uri(url)
            .headers(h -> h.setBearerAuth(token))
            .retrieve()
            .body(JsonNode.class);
    }

    public String healthCheck() {
        return "LookupService is ready";
    }
}
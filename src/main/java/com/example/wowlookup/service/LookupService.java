package com.example.wowlookup.service;

import org.springframework.stereotype.Service;
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

    public String getCharacterMplusScore(String characterName, String realm) {
        int seasonID = getCurrentMplusSeasonID();
        // Use the token to call Blizzard API and fetch character M+ score
        // This is a placeholder implementation
        JsonNode response = callBlizzardApi(
            "https://eu.api.blizzard.com/profile/wow/character/%s/%s/mythic-keystone-profile/season/%d?namespace=profile-eu&locale=en_EU",
            realm.toLowerCase(),
            characterName.toLowerCase(),
            seasonID
        );

            //System.out.println("API Response: " + response);

            Double mplusScore = response.path("mythic_rating").path("rating").asDouble();


        return "Character M+ Score for " + characterName + " on " + realm + ": " + mplusScore;
    }

    public String getCharacterProfessions(String characterName, String realm) {
        // Similar implementation to fetch character professions
        JsonNode response = callBlizzardApi(
            "https://eu.api.blizzard.com/profile/wow/character/%s/%s/professions?namespace=profile-eu&locale=en_EU",
            realm.toLowerCase(),
            characterName.toLowerCase()
        );

        String profOne = response.path("primaries").path(0).path("profession").path("name").asString();
        String profTwo = response.path("primaries").path(1).path("profession").path("name").asString();
    
        System.out.println("Professions API Response: " + response);        

        return "Character Professions for " + characterName + " on " + realm + ": " + profOne + ", " + profTwo;
    }

    private int getCurrentMplusSeasonID() {
        JsonNode response = callBlizzardApi(
            "https://us.api.blizzard.com/data/wow/mythic-keystone/season/index?namespace=dynamic-us&locale=en_US"
        );

        // Extract the current season ID from the response
        int currentSeasonID = response.path("current_season").path("id").asInt();

        return currentSeasonID;
    }

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
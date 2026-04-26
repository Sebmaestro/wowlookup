package com.example.wowlookup.service;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * Fetches the Mythic+ score of a character for the current season.
     * @param characterName
     * @param realm
     * @return M+ score as a Double, or null if not found
     */
    public Double getCharacterMplusScore(String characterName, String realm) {
        int seasonId = getCurrentMplusSeasonID();
        JsonNode response = callBlizzardApi(
            "https://eu.api.blizzard.com/profile/wow/character/%s/%s/mythic-keystone-profile/season/%d?namespace=profile-eu&locale=en_EU",
            realm.toLowerCase(),
            characterName.toLowerCase(),
            seasonId
        );

        return response.path("mythic_rating").path("rating").asDouble();
    }

    /**
     * Fetches the primary professions of a character.
     * @param characterName
     * @param realm
     * @return List of profession names, or an empty list if none found
     */
    public List<String> getCharacterProfessions(String characterName, String realm) {
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
    }

    /**
     * Fetches the current Mythic+ season ID from Blizzard's API.
     * @return Current season ID as an integer
     */
    private int getCurrentMplusSeasonID() {
        JsonNode response = callBlizzardApi(
            "https://us.api.blizzard.com/data/wow/mythic-keystone/season/index?namespace=dynamic-us&locale=en_US"
        );

        int currentSeasonID = response.path("current_season").path("id").asInt();

        return currentSeasonID;
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
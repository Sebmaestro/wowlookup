package com.example.wowlookup.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.example.wowlookup.dto.RaidInfo;
import com.example.wowlookup.dto.RaidProgressInfo;

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
     *
     * @param characterName
     * @param realm
     * @return M+ score as a String, "noScoreFound" if char exists but no score,
     * or "noChar" if character doesn't exist
     */
    public String getCharacterMplusScore(String characterName, String realm) {
        try {
            JsonNode response = callBlizzardApi(
                    "https://eu.api.blizzard.com/profile/wow/character/%s/%s/mythic-keystone-profile?namespace=profile-eu&locale=en_US",
                    realm.toLowerCase(),
                    characterName.toLowerCase()
            );

            JsonNode seasonArray = response.path("seasons");
            //System.out.println("Season array for " + characterName + ": " + seasonArray.toString());
            boolean currentSeason = false;
            int currentSeasonId = getCurrentSeasonId();
            for (JsonNode season : seasonArray) {
                if (season.path("id").asInt() == currentSeasonId) {
                    currentSeason = true;
                    break;
                }
            }

            if (!currentSeason) {
                System.out.println("SÄSONGEN ÄR INTE NUVARANDE VAAA");
                return "noScoreFound"; // No data for current season
            }

            //System.out.println("API response for M+ score lookup for " + characterName + ": " + response.toString());
            JsonNode ratingNode = response.path("current_mythic_rating").path("rating");

            String score = ratingNode.asString();
            return score;
        } catch (HttpClientErrorException.NotFound e) {
            return "noChar"; // Character or realm not found (404)
        }
    }

    /**
     * Fetches the primary professions of a character.
     *
     * @param characterName
     * @param realm
     * @return List of profession names, or an empty list if none found or
     * character doesn't exist
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
     * Fetches the raid progression of a character
     *
     * @param characterName
     * @param realm
     * @return List of raid progression info
     */
    public List<RaidProgressInfo> getCharacterRaidProgression(String characterName, String realm) {
        try {
            JsonNode response = callBlizzardApi(
                    "https://eu.api.blizzard.com/profile/wow/character/%s/%s/encounters/raids?namespace=profile-eu&locale=en_US",
                    realm.toLowerCase(),
                    characterName.toLowerCase()
            );

            JsonNode expansions = response.path("expansions");
            int bestDifficulty = 0;
            //String bestProgress = "";
            int progressDifficulty = 0;
            int progressBossCount = 0;
            int totalBossCount = 0;

            ArrayList<RaidProgressInfo> characterProgressionList = new ArrayList<>();
            int currentExpansionId = getCurrentExpansionId();

            for (JsonNode expansion : expansions) {
                System.out.println("Checking expansion: " + expansion.path("expansion").path("name").asString());
                if (expansion.path("expansion").path("id").asInt() == currentExpansionId) {
                    System.out.println("hello");
                    JsonNode raids = expansion.path("instances");

                    for (JsonNode raid : raids) {

                        JsonNode difficulties = raid.path("modes");
                        System.out.println("Checking difficulties for raid: " + raid.path("instance").path("name").asString());

                        for (JsonNode difficulty : difficulties) {
                            String status = difficulty.path("status").path(("name")).asString();
                            int difficultyRank = difficultyRank(difficulty.path("difficulty").path("name").asString());
                            System.out.println("Checking difficulty: " + difficulty.path("difficulty").path("name").asString() 
                            + " with status: " + status);

                            if (status.equals("Complete")) {
                                //bestDifficulty = Math.max(bestDifficulty, difficultyRank);
                                bestDifficulty = difficultyRank;
                                progressDifficulty = bestDifficulty;
                                progressBossCount = difficulty.path("progress").path("completed_count").asInt();

                                totalBossCount = difficulty.path("progress").path("total_count").asInt();
                            } else if (status.equals("In Progress")) {
                                progressBossCount = difficulty.path("progress").path("completed_count").asInt();
                                totalBossCount = difficulty.path("progress").path("total_count").asInt();
                                
                                if(progressBossCount > 0) {
                                    progressDifficulty = difficultyRank;
                                }                                
                            }

                        }
                        System.out.println("bestDifficulty fully cleared: " + bestDifficulty + ", progressBossCount: " + progressBossCount + ", totalBossCount: " + totalBossCount + ", progressDifficulty: " + progressDifficulty + " for raid: "
                                + raid.path("instance").path("name").asString());

                        characterProgressionList.add(new RaidProgressInfo(
                                raid.path("instance").path("name").asString(),
                                bestDifficulty,
                                progressBossCount,
                                totalBossCount,
                                progressDifficulty
                        ));
                    }

                    break;
                }
            }

            return characterProgressionList;

        } catch (HttpClientErrorException.NotFound e) {
            System.out.println("error vid fetch av raid progression: " + e.getMessage());
            return null;
        }
    }

    private int difficultyRank(String difficultyName) {
        return switch (difficultyName) {
            case "Raid Finder", "LFR" ->
                1;
            case "Normal" ->
                2;
            case "Heroic" ->
                3;
            case "Mythic" ->
                4;
            default ->
                0;
        };
    }

    /**
     * Fetches the names and boss counts of raids in the current expansion,
     * excluding Midnight.
     *
     * @return
     */
    public List<RaidInfo> getMidnightRaidInfo() {
        JsonNode response = callBlizzardApi(
                "https://us.api.blizzard.com/data/wow/journal-expansion/%d?namespace=static-us&locale=en_US",
                getCurrentExpansionId()
        );

        ArrayList<RaidInfo> raidInfos = new ArrayList<>();

        JsonNode raids = response.path("raids");
        for (JsonNode raid : raids) {
            String raidName = raid.path("name").asString();
            System.out.println("Raid name: " + raidName);

            String href = raid.path("key").path("href").asString();

            JsonNode nrOfBossesResponse = callBlizzardApi(href);
            JsonNode encounters = nrOfBossesResponse.path("encounters");

            if (!raidName.equals("Midnight")) {
                raidInfos.add(new RaidInfo(raidName, encounters.size()));
            }

        }
        for (RaidInfo info : raidInfos) {
            System.out.println("Raid: " + info.getName() + " with boss count: " + info.getBossCount());
        }
        return raidInfos;
    }

    /**
     * Helper method to call Blizzard's API with authentication and return the
     * response as a JsonNode.
     *
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

    /**
     * Helper method to fetch the current Mythic+ season ID from Blizzard's API.
     *
     * @return
     */
    private int getCurrentSeasonId() {
        JsonNode response = callBlizzardApi(
                "https://eu.api.blizzard.com/data/wow/mythic-keystone/season/index?namespace=dynamic-eu&locale=en_US"
        );
        return response.path("current_season").path("id").asInt();
    }

    private int getCurrentExpansionId() {
        JsonNode response = callBlizzardApi(
                "https://us.api.blizzard.com/data/wow/journal-expansion/index?namespace=static-us&locale=en_US"
        );

        JsonNode expansions = response.path("tiers");
        int max = 0;
        for (JsonNode expansion : expansions) {
            int id = expansion.path("id").asInt();
            if (id > max) {
                max = id;
            }
        }
        System.out.println("Current expansion ID: " + max);
        return max;
    }

    public String healthCheck() {
        return "LookupService is ready";
    }
}

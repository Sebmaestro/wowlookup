package com.example.wowlookup.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.wowlookup.dto.RaidInfo;
import com.example.wowlookup.dto.RaidProgressInfo;
import com.example.wowlookup.service.LookupService;

@RestController
@RequestMapping("/api/lookup")
public class LookupController {

    private final LookupService lookupService;

    public LookupController(LookupService lookupService) {
        this.lookupService = lookupService;
    }

    @GetMapping("/health")
    public String health() {
        return lookupService.healthCheck();
    }

    /**
     * Endpoint to fetch the Mythic+ score of a character for the current
     * season.
     *
     * @param characterName
     * @param realm
     * @return
     */
    @GetMapping("/mplusscore/{characterName}/{realm}")
    public ResponseEntity<?> getCharacterMplusScore(@PathVariable String characterName, @PathVariable String realm) {
        String score = lookupService.getCharacterMplusScore(characterName, realm);

        if ("noChar".equals(score)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Character not found: " + characterName + " on realm: " + realm);
        }

        if ("noScoreFound".equals(score)) {
            System.out.println("No M+ score found for character: " + characterName + " on realm: " + realm);
            return ResponseEntity.status(204).build();
        }

        return ResponseEntity.ok(score);
    }

    /**
     * Endpoint to fetch the primary professions of a character.
     *
     * @param characterName
     * @param realm
     * @return
     */
    @GetMapping("/professions/{characterName}/{realm}")
    public List<String> getCharacterProfessions(@PathVariable String characterName, @PathVariable String realm) {
        return lookupService.getCharacterProfessions(characterName, realm);
    }

    @GetMapping("/raidprogress/{characterName}/{realm}")
    public List<RaidProgressInfo> getCharacterRaidProgression(@PathVariable String characterName, @PathVariable String realm) {
        
        return lookupService.getCharacterRaidProgression(characterName, realm);        
    }

    /**
     * Endpoint to fetch the names of raids in the current expansion.
     * @return
     */
    @GetMapping("/raidnames") 
    public List<RaidInfo> getRaidNames() {
        System.out.println("Received request for raid names");
        return lookupService.getMidnightRaidInfo();
    }

    
    /**
     * Endpoint to fetch the current expansion ID.
     * @return
     */
    /*
    @GetMapping("/expansion/current")
    public int getCurrentExpansion() {
        return lookupService.getCurrentExpansionId();
    }
     */
}

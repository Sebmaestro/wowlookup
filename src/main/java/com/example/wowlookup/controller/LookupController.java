package com.example.wowlookup.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/professions/{characterName}/{realm}")
    public List<String> getCharacterProfessions(@PathVariable String characterName, @PathVariable String realm) {
        return lookupService.getCharacterProfessions(characterName, realm);
    }
}

package com.example.wowlookup.controller;

import java.util.List;

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
    public Double getCharacterMplusScore(@PathVariable String characterName, @PathVariable String realm) {
        return lookupService.getCharacterMplusScore(characterName, realm);
    }

    @GetMapping("/professions/{characterName}/{realm}")
    public List<String> getCharacterProfessions(@PathVariable String characterName, @PathVariable String realm) {
        return lookupService.getCharacterProfessions(characterName, realm);
    }
}
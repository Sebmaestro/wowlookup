package com.example.wowlookup.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.wowlookup.service.BlizzardTokenGenerator;

@RestController
@RequestMapping("/api/auth")
public class BlizzardAuthController {

    private final BlizzardTokenGenerator tokenGenerator;

    public BlizzardAuthController(BlizzardTokenGenerator tokenGenerator) {
        this.tokenGenerator = tokenGenerator;
    }

    @GetMapping("/accesstoken")
    public String getToken() {
        return tokenGenerator.getToken();
    }
}
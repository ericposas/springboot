package com.posas.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.posas.services.ProfileService;

@RestController
@RequestMapping(path = "/profiles")
public class ProfileController {

    @Autowired
    ProfileService profileService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listProfiles() {
        return ResponseEntity.ok(
                profileService.getAllUserProfiles());
    }

}

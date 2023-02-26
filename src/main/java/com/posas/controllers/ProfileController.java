package com.posas.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.posas.dtos.ProfileDTO;
import com.posas.services.ProfileService;

@RestController
@RequestMapping(path = "/profiles")
public class ProfileController {

    @Autowired
    ProfileService profileService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createProfile(@RequestBody ProfileDTO profile) {
        return ResponseEntity.ok(
                profileService.createUserProfile(profile));
    }

}

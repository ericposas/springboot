package com.posas.controllers;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.posas.services.ProfileService;

@RestController
@RequestMapping(path = "/user")
public class UserController {

    @Autowired
    private ProfileService profileService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createProfileDataFromAuthenticated(Principal principal) {
        return ResponseEntity.ok(
                profileService.createUserProfileFromJwtAuthData(principal));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getSelf(Principal principal) {
        return ResponseEntity.ok(
                profileService.getJwtProfileData(principal));
    }

}

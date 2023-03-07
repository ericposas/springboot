package com.posas.controllers;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.posas.dtos.AddressDTO;
import com.posas.services.ProfileService;
import com.stripe.exception.StripeException;

@RestController
@RequestMapping(path = "/user")
public class UserController {

    @Autowired
    private ProfileService profileService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getSelfProfile(Principal principal) throws StripeException {
        return ResponseEntity.ok(
                profileService.createUserProfileFromJwtAuthDataAndResponse(principal));
    }

    @GetMapping(path = "/jwtdata", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getSelfJwtData(Principal principal) throws StripeException {
        return ResponseEntity.ok(
                profileService.getJwtProfileData(principal));
    }

    @PostMapping(path = "/address/billing", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateAddressForUser(
            @RequestBody() AddressDTO body,
            Principal principal) throws StripeException {
        return ResponseEntity.ok(
                profileService.saveAddress(body, principal));
    }

    @PostMapping(path = "/address/shipping", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateShippingForUser(
            @RequestBody() AddressDTO body,
            Principal principal) throws StripeException {
        return ResponseEntity.ok(
                profileService.saveShippingAddress(body, principal));
    }

}

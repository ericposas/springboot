package com.posas.controllers;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.posas.dtos.AddressDTO;
import com.posas.dtos.AttributesDTO;
import com.posas.dtos.CreateUserFromJwtAuthResponseDTO;
import com.posas.dtos.PhoneNumberDTO;
import com.posas.entities.Profile;
import com.posas.services.ProfileService;
import com.stripe.exception.StripeException;

@RestController
@RequestMapping(path = "/user")
public class UserController {

    @Autowired
    @Qualifier("clientId")
    String clientId;

    @Autowired
    private ProfileService profileService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreateUserFromJwtAuthResponseDTO> getSelfProfile(Principal principal) throws StripeException {
        return ResponseEntity.ok(
                profileService.createUserProfileFromJwtAuthDataAndResponse(principal));
    }

    @PreAuthorize("hasRole('ROLE_user')")
    @GetMapping(path = "/scopes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AttributesDTO> ifScopeTestCreate(Principal principal) {
        return ResponseEntity.ok(
                profileService.getScopes(principal));
    }

    @PostMapping(path = "/address/billing", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Profile> updateAddressForUser(
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

    @PostMapping(path = "/phone", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updatePhoneNumber(Principal principal, @RequestBody PhoneNumberDTO body)
            throws StripeException {
        return ResponseEntity.ok(
                profileService.updatePhone(principal, body.getPhone()));
    }

}

package com.posas.controllers;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.posas.entities.Profile;
import com.posas.helpers.TokenHelpers;
import com.posas.repositories.ProfileRepository;
import com.posas.services.ProfileService;

import lombok.Builder;
import lombok.Data;

/**
 * {@summary} JWT Token extracted properties
 */
@Data
@Builder
class AuthResponseDTO {
    String sid;
    String iss;
    String name;
    Long authTime;
    Boolean emailVerified;
    List<String> roles;
    List<String> scopes;
    String givenName;
    String familyName;
    String email;
    String preferredUsername;
    String message;
}

@RestController
@RequestMapping(path = "/user")
public class UserController {

    @Autowired
    @Qualifier("clientId")
    private String clientId;

    @Autowired
    private ProfileService profileService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createProfileDataFromAuthenticated(Principal principal) {
        return ResponseEntity.ok(
                profileService.createUserProfileFromJwtAuthData(principal));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getSelf(Principal principal) {
        List<String> roles = TokenHelpers.getTokenResource(principal)
                .get(clientId)
                .get("roles");
        String scopesStr = (String) TokenHelpers.getTokenAttributes(principal).get("scope");
        List<String> scopes = List.of(scopesStr.split(" "));
        String name = TokenHelpers.getFromJwt(principal, "name");
        String preferredUsername = TokenHelpers.getFromJwt(principal, "preferred_username");
        String email = TokenHelpers.getFromJwt(principal, "email");

        System.out.print("\n\n");
        System.out.print("user: \n\n");
        System.out.print("preferred_username: \n");
        System.out.print(preferredUsername + "\n\n");
        System.out.print("email: \n");
        System.out.print(email);
        System.out.print("\n\n");

        String givenName = TokenHelpers.getFromJwt(principal, "given_name");
        String familyName = TokenHelpers.getFromJwt(principal, "family_name");
        String sid = TokenHelpers.getFromJwt(principal, "sid");
        String iss = TokenHelpers.getFromJwt(principal, "iss");
        Long authTime = (Long) TokenHelpers.getTokenAttributes(principal).get("auth_time");
        Boolean emailVerified = (Boolean) TokenHelpers.getTokenAttributes(principal).get("email_verified");

        return ResponseEntity.ok(AuthResponseDTO.builder()
                .iss(iss)
                .sid(sid)
                .authTime(authTime)
                .roles(roles)
                .scopes(scopes)
                .name(name)
                .email(email)
                .preferredUsername(preferredUsername)
                .emailVerified(emailVerified)
                .givenName(givenName)
                .familyName(familyName)
                .message("Details about you.")
                .build());
    }

    // @PreAuthorize("hasAuthority('SCOPE_test:view')")
    // @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    // public ResponseEntity<AttributesDTO> testView(Principal principal) {
    // List<String> roles = TokenHelpers.getTokenResource(principal)
    // .get(clientId)
    // .get("roles");
    // String scopesStr = (String)
    // TokenHelpers.getTokenAttributes(principal).get("scope");
    // List<String> scopes = List.of(scopesStr.split(" "));

    // return ResponseEntity.ok(
    // AttributesDTO.builder()
    // .scopes(scopes)
    // .roles(roles)
    // .message("You have the test:view scope")
    // .build());
    // }

}

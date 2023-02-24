package com.posas.controllers;

import java.security.Principal;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nimbusds.jose.shaded.gson.internal.LinkedTreeMap;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
class AttributesDTO {
    String role;
    String username;
    String scope;
    String message;
}

@Data
@Builder
class AccountPOJO {
    List<String> roles;
}

@Data
@Builder
class ResourceMapperPOJO {
    List<String> roles;
    AccountPOJO account;
}

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    @Qualifier("clientId")
    private String clientId;

    @GetMapping(path = "/anonymous", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AttributesDTO> getAnonymous() {
        AttributesDTO body = AttributesDTO.builder()
                .role(null)
                .username(null)
                .message("Hello Mr. Anon")
                .build();
        return ResponseEntity.ok(body);
    }

    @GetMapping(path = "/admin", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AttributesDTO> getAdmin(Principal principal) {
        JwtAuthenticationToken token = (JwtAuthenticationToken) principal;
        String userName = (String) token.getTokenAttributes().get("name");
        String userEmail = (String) token.getTokenAttributes().get("email");
        String userScope = (String) token.getTokenAttributes().get("scope");

        System.out.print("name: " + userName + "\n");
        System.out.print("username: " + userEmail + "\n");
        System.out.print("scope: " + userScope + "\n\n");

        System.out.print(
                "resource_access" +
                        token.getTokenAttributes()
                                .get("resource_access")
                        +
                        "\n\n");

        return ResponseEntity.ok(
                AttributesDTO.builder()
                        .role("Administrator")
                        .username(userName)
                        .scope(userScope)
                        .build());
    }

    @GetMapping("/user")
    public ResponseEntity<String> getUser(Principal principal) {
        JwtAuthenticationToken token = (JwtAuthenticationToken) principal;
        String userName = (String) token.getTokenAttributes().get("preferred_username");
        String userEmail = (String) token.getTokenAttributes().get("email");

        System.out.print(userName);
        System.out.print(userEmail);

        return ResponseEntity.ok("Role: User \nUser Name : " + userName + "\nUser Email : " + userEmail);
    }

    @SuppressWarnings("unchecked")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AttributesDTO> testCreate(Principal principal) {
        JwtAuthenticationToken token = (JwtAuthenticationToken) principal;
        String name = (String) token.getTokenAttributes().get("name");
        String scope = (String) token.getTokenAttributes().get("scope");
        LinkedTreeMap<String, LinkedTreeMap<String, List<String>>> resource = (LinkedTreeMap<String, LinkedTreeMap<String, List<String>>>) token
                .getTokenAttributes()
                .get("resource_access");

        try {
            List<String> roles = resource.get(clientId).get("roles");
            System.out.print("\n\n");
            System.out.print(token.getTokenAttributes());
            System.out.print("\n\n");
            System.out.print(name + " has " + roles.size() + " roles: " + roles);
            System.out.print("\n\n");

        } catch (Exception ex) {
            System.out.print(ex);
        }

        return ResponseEntity.ok(
                AttributesDTO.builder()
                        .scope(scope)
                        .message("You have the test:create scope")
                        .build());
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AttributesDTO> testView(Principal principal) {
        JwtAuthenticationToken token = (JwtAuthenticationToken) principal;
        String scope = (String) token.getTokenAttributes().get("scope");

        return ResponseEntity.ok(
                AttributesDTO.builder()
                        .scope(scope)
                        .message("You have the test:view scope")
                        .build());
    }

    @GetMapping(path = "/scope", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AttributesDTO> ifScopeTestCreate(Principal principal) {
        JwtAuthenticationToken token = (JwtAuthenticationToken) principal;
        return ResponseEntity.ok(
                AttributesDTO.builder()
                        .scope((String) token.getTokenAttributes().get("scope"))
                        .build());
    }

}
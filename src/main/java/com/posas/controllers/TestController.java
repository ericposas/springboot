package com.posas.controllers;

import java.security.Principal;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

@RestController
@RequestMapping("/test")
public class TestController {

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
                .get("resource_access") +
            "\n\n"
        );

        return ResponseEntity.ok(
            AttributesDTO.builder()
                .role("Administrator")
                .username(userName)
                .scope(userScope)
                .build()
        );
    }
    
    @GetMapping("/user")
    public ResponseEntity<String> getUser(Principal principal) {
        JwtAuthenticationToken token = (JwtAuthenticationToken) principal;
        String userName = (String) token.getTokenAttributes().get("name");
        String userEmail = (String) token.getTokenAttributes().get("email");

        System.out.print(userName);
        System.out.print(userEmail);

        return ResponseEntity.ok("Role: User \nUser Name : " + userName + "\nUser Email : " + userEmail);
    }

    @GetMapping(path = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AttributesDTO> createTestScope() {
        return ResponseEntity.ok(
            AttributesDTO.builder()
                .message("You have the test:create scope")
                .build()
        );
    }

    @GetMapping(path = "/view", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AttributesDTO> createViewScope() {
        return ResponseEntity.ok(
            AttributesDTO.builder()
                .message("You have the test:view scope")
                .build()
        );
    }

    @GetMapping(path = "/scope", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AttributesDTO> ifScopeTestCreate(Principal principal) {
        JwtAuthenticationToken token = (JwtAuthenticationToken) principal;
        return ResponseEntity.ok(
            AttributesDTO.builder()
                .scope((String) token.getTokenAttributes().get("scope"))
                .build()
        );
    }


}
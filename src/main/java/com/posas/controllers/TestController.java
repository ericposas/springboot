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
class GetAnonDTO {
    String message;
}

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping(path = "/anonymous", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetAnonDTO> getAnonymous() {
        GetAnonDTO body = GetAnonDTO.builder()
                .message("Hello Mr. Anon")
                .build();
        return ResponseEntity.ok(body);
    }

    @GetMapping("/admin")
    public ResponseEntity<String> getAdmin(Principal principal) {
        JwtAuthenticationToken token = (JwtAuthenticationToken) principal;
        String userName = (String) token.getTokenAttributes().get("name");
        String userEmail = (String) token.getTokenAttributes().get("email");

        System.out.print(userName);
        System.out.print(userEmail);

        return ResponseEntity.ok("Hello Admin \nUser Name : " + userName + "\nUser Email : " + userEmail);
    }

    @GetMapping("/user")
    public ResponseEntity<String> getUser(Principal principal) {
        JwtAuthenticationToken token = (JwtAuthenticationToken) principal;
        String userName = (String) token.getTokenAttributes().get("name");
        String userEmail = (String) token.getTokenAttributes().get("email");

        System.out.print(userName);
        System.out.print(userEmail);

        return ResponseEntity.ok("Hello User \nUser Name : " + userName + "\nUser Email : " + userEmail);
    }

}
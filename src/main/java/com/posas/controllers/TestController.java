package com.posas.controllers;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.posas.helpers.TokenHelpers;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
class AttributesDTO {
    List<String> roles;
    List<String> scopes;
    String username;
    String message;
}

@RestController
@RequestMapping("/test")
@PropertySource(ignoreResourceNotFound = true, value = "classpath:some.properties")
public class TestController {

    @Value("${url}")
    String testProperty;

    @Autowired
    @Qualifier("clientId")
    private String clientId;

    @GetMapping(path = "/readproperties", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getTestValue() {
        return ResponseEntity.ok("{ \"value\": \"" + testProperty + "\" }");
    }

    @GetMapping(path = "/anonymous", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AttributesDTO> getAnonymous() {
        AttributesDTO body = AttributesDTO.builder()
                .roles(null)
                .username(null)
                .message("Hello Mr. Anon")
                .build();
        return ResponseEntity.ok(body);
    }

    @PreAuthorize("hasRole('admin') or hasAuthority('SCOPE_test:create')")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AttributesDTO> testCreate(Principal principal) {
        List<String> roles = TokenHelpers.getTokenResource(principal)
                .get(clientId)
                .get("roles");
        Map<String, Object> tokenAttrs = TokenHelpers.getTokenAttributes(principal);

        System.out.print("\n\n");
        System.out.print(tokenAttrs);
        System.out.print("\n\n");
        System.out.print(tokenAttrs.get("name") + " has " + roles.size() + " roles: " + roles);
        System.out.print("\n\n");

        String scopesStr = (String) tokenAttrs.get("scope");
        List<String> scopes = List.of(scopesStr.split(" "));

        return ResponseEntity.ok(
                AttributesDTO.builder()
                        .scopes(scopes)
                        .roles(roles)
                        .message("You have the test:create scope")
                        .build());
    }

    @PreAuthorize("hasAuthority('SCOPE_test:view')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AttributesDTO> testView(Principal principal) {
        List<String> roles = TokenHelpers.getTokenResource(principal)
                .get(clientId)
                .get("roles");
        String scopesStr = (String) TokenHelpers.getTokenAttributes(principal).get("scope");
        List<String> scopes = List.of(scopesStr.split(" "));

        return ResponseEntity.ok(
                AttributesDTO.builder()
                        .scopes(scopes)
                        .roles(roles)
                        .message("You have the test:view scope")
                        .build());
    }

    @PreAuthorize("hasAnyRole({'ROLE_user'})")
    @GetMapping(path = "/scopes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AttributesDTO> ifScopeTestCreate(Principal principal) {
        return ResponseEntity.ok(
                AttributesDTO.builder()
                        .roles(TokenHelpers.getTokenResource(principal).get(clientId)
                                .get("roles"))
                        .scopes(List.of(((String) TokenHelpers.getTokenAttributes(principal)
                                .get("scope")).split(" ")))
                        .username(TokenHelpers.getFromJwt(principal, "preferred_username"))
                        .message("/api/test/scopes")
                        .build());
    }

}
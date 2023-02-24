package com.posas.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
class ResponseDTO {
    String message;
}

@RestController
@RequestMapping("/secured")
public class SecuredController {

    @GetMapping
    @PreAuthorize("hasRole('ROLE_admin') and hasAuthority('SCOPE_test:view')")
    public ResponseEntity<?> testSecuredGet() {
        ResponseDTO response = ResponseDTO.builder()
                .message("route/endpoint is accessible")
                .build();
        return ResponseEntity.ok(response);
    }

}

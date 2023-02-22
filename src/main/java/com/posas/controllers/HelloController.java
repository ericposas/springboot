package com.posas.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.Data;

@Data
class HelloDTO {
    String message;
}

@RestController
public class HelloController {

    @GetMapping("/hello")
    public ResponseEntity<?> getHello() {
        HelloDTO response = new HelloDTO();
        response.setMessage("This is a test \"helloooooo...\"");
        return ResponseEntity.ok(response);
    }

}

package com.posas.controllers;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.posas.dtos.LineItemsBodyDTO;
import com.posas.services.CheckoutSessionService;
import com.stripe.exception.StripeException;

@RestController
@RequestMapping("/checkout")
public class CheckoutController {

    @Autowired
    CheckoutSessionService checkoutSessionService;

    @GetMapping(path = "/success", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> successPage() {
        return ResponseEntity.ok("{ \"checkout\": \"success\" }");
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createCheckoutSession(Principal principal, @RequestBody LineItemsBodyDTO body)
            throws StripeException {
        return ResponseEntity.ok(
                checkoutSessionService.createCheckoutSession(principal, body).toJson());
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<?> retrieveCheckoutSessionById(@PathVariable("sessionId") String sessionId)
            throws StripeException {
        return ResponseEntity.ok(
                checkoutSessionService.getCheckoutSession(sessionId));
    }

}

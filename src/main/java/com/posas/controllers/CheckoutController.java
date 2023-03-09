package com.posas.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.posas.dtos.LineItemsBodyDTO;
import com.posas.services.StripeCheckoutSession;
import com.stripe.exception.StripeException;

@RestController
@RequestMapping("/checkout")
public class CheckoutController {

    @Autowired
    StripeCheckoutSession checkoutSessionService;

    @GetMapping(path = "/success", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> successPage() {
        return ResponseEntity.ok("{ \"checkout\": \"success\" }");
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createCheckoutSession(@RequestBody LineItemsBodyDTO body) throws StripeException {
        return ResponseEntity.ok(
                checkoutSessionService.createCheckoutSession(body).toJson());
    }

}

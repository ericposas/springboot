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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.posas.dtos.ListOfProductIds;
import com.posas.services.CheckoutSessionService;
import com.stripe.exception.StripeException;

@RestController
@RequestMapping("/checkout")
public class CheckoutController {

    @Autowired
    CheckoutSessionService checkoutSessionService;

    @GetMapping(path = "/success", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> successPage() {
        return ResponseEntity.ok("{ \"checkout\": \"success\" }");
    }

    @GetMapping(path = "/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> cancelPage() {
        return ResponseEntity.ok("{ \"checkout\": \"canceled.\" }");
    }

    @PostMapping(path = "/guest", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createCheckoutSessionGuest(@RequestBody ListOfProductIds productIds)
            throws StripeException {
        return ResponseEntity.ok(checkoutSessionService.createCheckoutSession(productIds).toJson());
    }

    @PostMapping(path = "/customer", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createCheckoutSessionCustomer(Principal principal,
            @RequestBody ListOfProductIds productIds)
            throws StripeException {
        return ResponseEntity.ok(checkoutSessionService.createCheckoutSession(principal, productIds).toJson());
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<String> retrieveCheckoutSessionById(@PathVariable("sessionId") String sessionId)
            throws StripeException {
        return ResponseEntity.ok(
                checkoutSessionService.getCheckoutSession(sessionId).toJson());
    }

    @GetMapping(path = "/sessions/{sessionId}/lineitems")
    public ResponseEntity<?> retrieveCheckoutSessionLineItems(
            @PathVariable("sessionId") String sessionId,
            @RequestParam(value = "idsOnly", required = false) Boolean idsOnly,
            @RequestParam(value = "productObjects", required = false) String productObjects)
            throws StripeException {
        if (idsOnly != null && idsOnly == true) {
            return ResponseEntity.ok(
                    checkoutSessionService.getCheckoutSessionLineItems(sessionId, true));
        }
        if (productObjects != null) {
            return ResponseEntity.ok(
                    checkoutSessionService.getCheckoutSessionLineItems(sessionId, productObjects));
        }
        return ResponseEntity.ok(
                checkoutSessionService.getCheckoutSessionLineItems(sessionId));
    }

}

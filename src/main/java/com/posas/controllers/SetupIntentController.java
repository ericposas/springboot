package com.posas.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.SetupIntent;

@RestController
@RequestMapping("/setup")
public class SetupIntentController {

    @Value("${stripe.secret-key}")
    String secretKey;

    @GetMapping("/intent/{intent}")
    public ResponseEntity<?> retrievSetupIntent(@PathVariable(value = "intent") String intent) throws StripeException {
        Stripe.apiKey = secretKey;
        System.out.print("\n\n");
        System.out.print(intent);
        System.out.print("\n\n");
        return ResponseEntity.ok(
                SetupIntent.retrieve(intent).toJson());
    }

}

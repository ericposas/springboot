package com.posas.controllers;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.posas.entities.Profile;
import com.posas.helpers.TokenHelpers;
import com.posas.repositories.ProfileRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;

@RestController
@RequestMapping("/charge")
public class ChargePaymentIntentController {

    @Value("${stripe.secret-key}")
    String secretKey;

    @Autowired
    ProfileRepository profileRepo;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createPaymentIntent(Principal principal)
            throws StripeException {

        return ResponseEntity.ok(charge(principal));
    }

    // TODO: Move to service
    public String charge(Principal principal) throws StripeException {
        Profile profile = profileRepo.findByEmail(
                TokenHelpers.getFromJwt(principal, "email"));

        Stripe.apiKey = secretKey;
        Map<String, Object> automaticPaymentMethods = new HashMap<>();
        automaticPaymentMethods.put("enabled", true);
        Map<String, Object> params = new HashMap<>();
        params.put("amount", 5000);
        params.put("currency", "usd");
        params.put("confirm", true);
        params.put("customer", profile.getStripeCustomerId());
        Object[] pmtMthds = profile.getStripePaymentMethodIds().toArray();
        params.put("payment_method", pmtMthds[pmtMthds.length - 1]);

        PaymentIntent paymentIntent = PaymentIntent.create(params);
        return paymentIntent.toJson();
    }

}

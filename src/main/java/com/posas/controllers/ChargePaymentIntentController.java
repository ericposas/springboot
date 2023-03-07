package com.posas.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;

import lombok.Data;

@Data
class PaymentDTO {
    String customerId;
    String paymentMethodId;
}

@RestController
@RequestMapping("/charge")
public class ChargePaymentIntentController {

    @Value("${stripe.secret-key}")
    String secretKey;

    // @GetMapping("/{intent}")
    // public ResponseEntity<?> retrievSetupIntent(@PathVariable(value = "intent")
    // String intent) throws StripeException {
    // Stripe.apiKey = secretKey;
    // return ResponseEntity.ok(
    // SetupIntent.retrieve(intent).toJson());
    // }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createPaymentIntent(@RequestBody() PaymentDTO body) throws StripeException {
        Stripe.apiKey = secretKey;

        Map<String, Object> automaticPaymentMethods = new HashMap<>();
        automaticPaymentMethods.put("enabled", true);
        Map<String, Object> params = new HashMap<>();
        params.put("amount", 2000);
        params.put("currency", "usd");
        params.put("confirm", true);
        params.put("customer", body.getCustomerId());
        params.put("payment_method", body.getPaymentMethodId());

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        return ResponseEntity.ok(paymentIntent.toJson());
    }

}

package com.posas.controllers;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.posas.dtos.ChargeDTO;
import com.posas.exceptions.ChargeException;
import com.posas.services.ChargePaymentService;
import com.stripe.exception.StripeException;

@RestController
@RequestMapping("/charge")
public class ChargePaymentIntentController {

    @Value("${stripe.secret-key}")
    String secretKey;

    @Autowired
    ChargePaymentService chargePaymentService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createPaymentIntent(@RequestBody() ChargeDTO body, Principal principal)
            throws StripeException, ChargeException {
        return ResponseEntity.ok(chargePaymentService.charge(body, principal));
    }

}

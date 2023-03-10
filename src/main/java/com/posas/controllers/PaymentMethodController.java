package com.posas.controllers;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.posas.dtos.CardDTO;
import com.posas.services.PaymentMethodService;
import com.stripe.exception.StripeException;

@RestController
@RequestMapping("/payments")
public class PaymentMethodController {

    @Autowired
    PaymentMethodService paymentMethodService;

    @PostMapping("/methods")
    public ResponseEntity<?> createPaymentMethod(Principal principal, @RequestBody() CardDTO cardDto)
            throws StripeException {
        return ResponseEntity.ok(paymentMethodService.createPaymentMethod(principal, cardDto).toJson());
    }

    @GetMapping("/methods")
    public ResponseEntity<?> retrievePaymentMethod(Principal principal)
            throws StripeException {
        return ResponseEntity.ok(paymentMethodService.listPaymentMethodsForUser(principal));
    }

}

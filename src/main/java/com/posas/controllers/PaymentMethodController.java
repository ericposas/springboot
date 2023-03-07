package com.posas.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.posas.dtos.CardDTO;
import com.posas.services.StripePaymentMethodService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentMethod;

@RestController
@RequestMapping("/paymentmethod")
public class PaymentMethodController {

    @Autowired
    StripePaymentMethodService paymentMethodService;

    @PostMapping("/{customerId}")
    public ResponseEntity<?> createPaymentMethod(@PathVariable("customerId") String custId,
            @RequestBody() CardDTO cardDto)
            throws StripeException {
        return ResponseEntity.ok(paymentMethodService.createPaymentMethod(custId, cardDto).toJson());
    }

    @GetMapping("/{paymentMethodId}")
    public ResponseEntity<?> retrievePaymentMethod(@PathVariable("paymentMethodId") String paymentMethodId)
            throws StripeException {
        return ResponseEntity.ok(PaymentMethod.retrieve(paymentMethodId).toJson());
    }

}

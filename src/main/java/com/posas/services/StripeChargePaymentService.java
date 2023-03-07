package com.posas.services;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.posas.dtos.ChargeDTO;
import com.posas.entities.Profile;
import com.posas.exceptions.ChargeException;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;

@Service
public class StripeChargePaymentService {

    @Value("${stripe.secret-key}")
    String secretKey;

    @Autowired
    ProfileService profileService;

    public String charge(ChargeDTO body, Principal principal) throws StripeException, ChargeException {
        Stripe.apiKey = secretKey;
        Profile profile = profileService.getProfile(principal);
        Map<String, Object> automaticPaymentMethods = new HashMap<>();
        automaticPaymentMethods.put("enabled", true);
        Map<String, Object> params = new HashMap<>();
        Integer amountToCharge = body.getAmount();
        if (amountToCharge == null) {
            throw new ChargeException("No amount to charge included in request");
        }
        params.put("amount", amountToCharge);
        params.put("currency", "usd");
        params.put("confirm", true);
        params.put("customer", profile.getStripeCustomerId());
        try {
            Object[] pmtMthds = profile.getStripePaymentMethodIds().toArray();
            String paymentMethodToCharge = (String) pmtMthds[pmtMthds.length - 1];
            params.put("payment_method", paymentMethodToCharge);
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            return paymentIntent.toJson();
        } catch (Exception ex) {
            System.out.print("User has no saved payment methods");
        }
        return null;
    }

}

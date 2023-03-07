package com.posas.services;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.posas.dtos.CardDTO;
import com.posas.entities.Profile;
import com.posas.repositories.ProfileRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentMethod;
import com.stripe.param.PaymentMethodAttachParams;

@Service
public class StripePaymentMethodService {

    @Value("${stripe.secret-key}")
    String secretKey;

    @Autowired
    ProfileRepository profileRepo;

    public PaymentMethod createPaymentMethod(String custId, CardDTO card) throws StripeException {
        Stripe.apiKey = secretKey;

        Map<String, Object> cardParams = new HashMap<>();
        cardParams.put("number", card.getCardNumber());
        cardParams.put("exp_month", card.getExpMonth());
        cardParams.put("exp_year", card.getExpYear());
        cardParams.put("cvc", card.getCvc());

        Map<String, Object> params = new HashMap<>();
        params.put("type", "card");
        params.put("card", cardParams);

        PaymentMethod paymentMethod = PaymentMethod.create(params);
        String pmtMthdId = paymentMethod.getId();

        Map<String, Object> custParams = new HashMap<>();
        custParams.put("customer", custId);
        PaymentMethod pmtMthd = PaymentMethod.retrieve(pmtMthdId);
        pmtMthd.attach(PaymentMethodAttachParams.builder().setCustomer(custId).build());
        
        Profile profile = profileRepo.findByStripeCustomerId(custId);
        Set<String> paymentMethods = profile.getStripePaymentMethodIds();
        if (paymentMethods == null) {
            paymentMethods = new HashSet<String>();
        }
        paymentMethods.add(paymentMethod.getId());
        profile.setStripePaymentMethodIds(paymentMethods);
        profileRepo.save(profile);

        return paymentMethod;
    }

}

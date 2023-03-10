package com.posas.services;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
public class PaymentMethodService {

    @Value("${stripe.secret-key}")
    String stripeApiKey;

    @Autowired
    ProfileRepository profileRepo;

    @Autowired
    ProfileService profileService;

    public List<Object> listPaymentMethodsForUser(Principal principal) throws StripeException {
        Stripe.apiKey = stripeApiKey;
        Profile profile = profileService.getProfile(principal);
        List<String> paymentMethodIds = new ArrayList<>(profile.getStripePaymentMethodIds());
        List<Object> stripePaymentMethods = paymentMethodIds.stream()
                .map((id) -> {
                    try {
                        Map<String, String> map = new HashMap<>();
                        map.put("paymentMethod-Id", id);
                        map.put("paymentMethod-Object",
                                PaymentMethod.retrieve(id).toJson());
                        return map;
                    } catch (StripeException stEx) {
                        System.out.print(stEx);
                    }
                    return null;
                })
                .collect(Collectors.toList());
        return stripePaymentMethods;
    }

    public PaymentMethod createPaymentMethod(Principal principal, CardDTO card) throws StripeException {
        Stripe.apiKey = stripeApiKey;

        Profile profile = profileService.getProfile(principal);
        String custId = profile.getStripeCustomerId();

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

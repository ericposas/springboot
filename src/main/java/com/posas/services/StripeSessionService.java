package com.posas.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;

@Service
public class StripeSessionService {

    @Value("${spring.profiles.active}")
    String activeProfile;

    @Value("${stripe.secret-key}")
    String secretKey;

    @Autowired
    StripeProductService productAndPriceService;

    public Session createStripeCheckout() throws StripeException {
        Stripe.apiKey = secretKey;
        List<Object> lineItems = new ArrayList<>();
        Map<String, Object> lineItem1 = new HashMap<>();
        lineItem1.put("price", productAndPriceService.createPrice().getId());
        lineItem1.put("quantity", 2);
        lineItems.add(lineItem1);
        Map<String, Object> params = new HashMap<>();
        System.out.print("\n\n");
        System.out.print(activeProfile);
        System.out.print("\n\n");
        params.put(
                "success_url",
                activeProfile == "dev" ? "http://localhost/checkout/success"
                        : "https://webcommerce.live/checkout/success");
        params.put("line_items", lineItems);
        params.put("mode", "payment");

        Session session = Session.create(params);
        return session;
    }

}

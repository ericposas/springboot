package com.posas.services;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.posas.dtos.LineItemsBodyDTO;
import com.posas.entities.Profile;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.checkout.Session;

@Service
public class CheckoutSessionService {

    @Value("${stripe.secret-key}")
    private String stripeApiKey;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Autowired
    ProfileService profileService;

    public Session createCheckoutSession(Principal principal, LineItemsBodyDTO body) throws StripeException {
        Stripe.apiKey = stripeApiKey;
        Profile profile = profileService.getProfile(principal);
        // First, get list of products from DB from client body req.
        List<Object> line_items = new ArrayList<>();
        body.getProducts().stream()
                .forEach((product) -> {
                    com.stripe.model.Product stripeProduct = new com.stripe.model.Product();
                    Price price = new Price();
                    try {
                        stripeProduct = com.stripe.model.Product
                                .retrieve(product.getStripeProductId());
                        price = Price.retrieve(stripeProduct.getDefaultPrice());
                    } catch (StripeException se) {
                        System.out.print(se);
                    }
                    Map<String, Object> line_item = new HashMap<>();
                    line_item.put("price", price.getId());
                    line_item.put("quantity", (long) 1);
                    line_items.add(line_item);
                });

        Map<String, Object> params = new HashMap<>();
        params.put(
                "success_url",
                (activeProfile.trim().equals("dev") ? "http://localhost"
                        : "https://webcommerce.live") + "/api/checkout/success");
        params.put("line_items", line_items);
        params.put("customer", profile.getStripeCustomerId());
        params.put("mode", "payment");

        Session session = Session.create(params);
        return session;
    }

    public String getCheckoutSession(String sessionId) throws StripeException {
        Stripe.apiKey = stripeApiKey;
        return Session.retrieve(sessionId).toJson();
    }

}

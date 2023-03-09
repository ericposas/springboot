package com.posas.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.posas.dtos.LineItemsBodyDTO;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.checkout.Session;

@Service
public class StripeCheckoutSession {

    @Value("${stripe.secret-key}")
    private String stripeApiKey;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    public Session createCheckoutSession(LineItemsBodyDTO body) throws StripeException {
        Stripe.apiKey = stripeApiKey;
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

        System.out.print("\n\n");
        System.out.print(activeProfile);
        System.out.print("\n\n");

        params.put(
                "success_url",
                (activeProfile.trim().equals("dev") ? "http://localhost"
                        : "https://webcommerce.live") + "/api/checkout/success");
        params.put("line_items", line_items);
        params.put("mode", "payment");

        Session session = Session.create(params);
        return session;
    }

}

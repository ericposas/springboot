package com.posas.services;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.posas.dtos.ListOfProductIds;
import com.posas.entities.Product;
import com.posas.entities.Profile;
import com.posas.helpers.BaseURL;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.LineItemCollection;
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

    @Autowired
    ProductsService productService;

    public Map<String, Object> createSessionParams(ListOfProductIds body) throws StripeException {
        Stripe.apiKey = stripeApiKey;
        // First, get list of products from DB from client body req.
        List<Object> line_items = new ArrayList<>();
        body.getProductIds().stream()
                .forEach((id) -> {
                    com.stripe.model.Product stripeProduct = new com.stripe.model.Product();
                    Price price = new Price();
                    try {
                        Product dbProduct = productService.getDbProduct(id);
                        stripeProduct = com.stripe.model.Product.retrieve(dbProduct.getStripeProductId());
                        price = Price.retrieve(stripeProduct.getDefaultPrice());
                    } catch (StripeException ex) {
                        System.out.print(ex);
                    }
                    Map<String, Object> line_item = new HashMap<>();
                    line_item.put("price", price.getId());
                    line_item.put("quantity", (long) 1);
                    line_items.add(line_item);
                });

        Map<String, Object> params = new HashMap<>();
        params.put("success_url", BaseURL.getBaseUrl(activeProfile, "/api/checkout/success"));
        params.put("cancel_url", BaseURL.getBaseUrl(activeProfile, "/api/checkout/cancel"));
        params.put("line_items", line_items);
        params.put("mode", "payment");
        return params;
    }

    public Session createCheckoutSession(ListOfProductIds productIds) throws StripeException {
        Stripe.apiKey = stripeApiKey;
        Map<String, Object> params = createSessionParams(productIds);
        Session session = Session.create(params);
        return session;
    }

    public Session createCheckoutSession(Principal principal, ListOfProductIds productIds) throws StripeException {
        Stripe.apiKey = stripeApiKey;
        Map<String, Object> params = createSessionParams(productIds);
        Profile profile = profileService.getProfile(principal);
        if (profile != null)
            params.put("customer", profile.getStripeCustomerId());
        Session session = Session.create(params);
        return session;
    }

    public Session getCheckoutSession(String sessionId) throws StripeException {
        Stripe.apiKey = stripeApiKey;
        return Session.retrieve(sessionId);
    }

    public LineItemCollection getCheckoutSessionLineItems(String sessionId) throws StripeException {
        Stripe.apiKey = stripeApiKey;
        Session session = getCheckoutSession(sessionId);
        return session.listLineItems();
    }

}

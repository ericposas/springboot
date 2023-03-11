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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.posas.dtos.ListOfProductIds;
import com.posas.dtos.StripeProductObject;
import com.posas.entities.Product;
import com.posas.entities.Profile;
import com.posas.helpers.BaseURL;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.LineItem;
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
        List<Map<String, Object>> line_items = new ArrayList<>();
        Map<Long, Long> item_counts = new HashMap<>();
        body.getProductIds().stream()
                .forEach((id) -> {
                    try {
                        com.stripe.model.Product stripeProduct = new com.stripe.model.Product();
                        Product dbProduct = productService.getDbProduct(id);
                        stripeProduct = com.stripe.model.Product.retrieve(dbProduct.getStripeProductId());
                        final Price price = Price.retrieve(stripeProduct.getDefaultPrice());
                        if (item_counts.get(id) != null) {
                            item_counts.put(id, item_counts.get(id) + 1);
                        } else {
                            item_counts.put(id, (long) 1);
                        }
                        Map<String, Object> line_item = new HashMap<>();
                        if (item_counts.get(id) > 1) {
                            for (int i = 0; i < line_items.size(); i++) {
                                var element = line_items.get(i);
                                if (element.get("price").equals(price.getId())) {
                                    line_items.remove(element);
                                }
                            }
                        }
                        line_item.put("price", price.getId());
                        line_item.put("quantity", item_counts.get(id));
                        line_items.add(line_item);
                    } catch (StripeException ex) {
                        System.out.print(ex);
                    }
                });

        Map<String, Object> params = new HashMap<>();
        params.put("success_url", BaseURL.getBaseUrl(activeProfile, "/api/checkout/success"));
        params.put("cancel_url", BaseURL.getBaseUrl(activeProfile, "/api/checkout/cancel"));
        Set<Map<String, Object>> line_items_set = new HashSet<>(line_items);
        line_items.clear();
        line_items.addAll(line_items_set);
        params.put("line_items", line_items);
        params.put("mode", "payment");

        System.out.print("\n\n");
        System.out.print(item_counts);
        System.out.print("\n\n");

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

    // TODO: Create method to retrieve an existing session and update the items,
    // add/remove
    public Session updateCheckoutSession(String sessionId, ListOfProductIds productIds) throws StripeException {

        Stripe.apiKey = stripeApiKey;
        // Session session = Session.retrieve(sessionId);
        // session

        return new Session();
    }

    // TODO: Create method to retrieve all session products as a List and either add
    // or remove from the List

    // TODO: Need to be able to add more items to the productIds array

    public Session getCheckoutSession(String sessionId) throws StripeException {
        Stripe.apiKey = stripeApiKey;
        return Session.retrieve(sessionId);
    }

    public List<LineItem> getCheckoutSessionLineItems(String sessionId) throws StripeException {
        Stripe.apiKey = stripeApiKey;
        Session session = getCheckoutSession(sessionId);
        return session.listLineItems().getData();
    }

    public Map<String, List<String>> getCheckoutSessionLineItems(String sessionId, Boolean idsOnly)
            throws StripeException {
        Stripe.apiKey = stripeApiKey;
        Session session = getCheckoutSession(sessionId);
        List<String> productIds = session.listLineItems().getData()
                .stream()
                .map((li) -> {
                    return li.getPrice().getProduct();
                })
                .collect(Collectors.toList());
        Map<String, List<String>> json = new HashMap<>();
        json.put("productIds", productIds);
        return json;
    }

    public Map<String, List<StripeProductObject>> getCheckoutSessionLineItems(String sessionId,
            String productObjects)
            throws StripeException {
        Stripe.apiKey = stripeApiKey;
        Session session = getCheckoutSession(sessionId);
        List<StripeProductObject> products = session.listLineItems().getData()
                .stream()
                .map((li) -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        var result = mapper.readValue(
                                com.stripe.model.Product.retrieve(li.getPrice().getProduct()).toJson(),
                                StripeProductObject.class);
                        return result;
                    } catch (StripeException | JsonProcessingException ex) {
                        System.out.print("\n\n");
                        System.out.print(ex);
                        System.out.print("\n\n");
                        return null;
                    }
                })
                .collect(Collectors.toList());
        Map<String, List<StripeProductObject>> json = new HashMap<>();
        json.put("productObjects", products);
        return json;
    }

}

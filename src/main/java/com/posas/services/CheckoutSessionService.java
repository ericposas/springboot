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
import com.posas.dtos.ListOfProductIdsWrapper;
import com.posas.dtos.StripeProductObject;
import com.posas.entities.Product;
import com.posas.entities.Profile;
import com.posas.helpers.BaseURL;
import com.posas.repositories.ProductRepository;
import com.posas.repositories.ProfileRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.LineItem;
import com.stripe.model.Price;
import com.stripe.model.checkout.Session;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
class ProductIdsObjectDTO {
    private List<Long> dbProductIds;
    private String checkoutUrl;
}

@Service
public class CheckoutSessionService {

    @Value("${stripe.secret-key}")
    private String stripeApiKey;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Autowired
    ProfileService profileService;

    @Autowired
    ProfileRepository profileRepo;

    @Autowired
    ProductsService productService;

    @Autowired
    ProductRepository productsRepo;

    public Map<String, Object> createSessionParams(ListOfProductIdsWrapper body) throws StripeException {
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

    ////////////////////////////
    // CREATE CHECKOUT SESSION
    ////////////////////////////
    public Session createCheckoutSession(ListOfProductIdsWrapper productIdsWrapper) throws StripeException {
        Stripe.apiKey = stripeApiKey;
        Map<String, Object> params = createSessionParams(productIdsWrapper);
        Session session = Session.create(params);
        return session;
    }

    public Session createCheckoutSession(Principal principal, ListOfProductIdsWrapper productIdsWrapper)
            throws StripeException {
        Stripe.apiKey = stripeApiKey;
        Map<String, Object> params = createSessionParams(productIdsWrapper);
        Profile profile = profileService.getProfile(principal);
        if (profile != null) {
            params.put("customer", profile.getStripeCustomerId());
        }
        Session session = Session.create(params);
        if (profile != null) {
            System.out.print("\n\n");
            System.out.print("sessionId: " + session.getId() + "\n");
            System.out.print("\n\n");
            profile.setLatestCheckoutSession(session.getId());
            profileRepo.save(profile);
        }
        return session;
    }

    ////////////////////////////////////////////
    // REMOVE PRODUCT(S) FROM CHECKOUT SESSION
    ////////////////////////////////////////////
    private ListOfProductIdsWrapper removeProductListFromCheckoutSessionCommon(String sessionId,
            ListOfProductIdsWrapper productIdsWrapper)
            throws StripeException {
        Stripe.apiKey = stripeApiKey;
        ProductIdsObjectDTO productIdsObject = getCheckoutSessionLineItems(sessionId, true);
        List<Long> dbProductIds = productIdsObject.getDbProductIds();
        dbProductIds.removeAll(productIdsWrapper.getProductIds());
        ListOfProductIdsWrapper updatedIds = new ListOfProductIdsWrapper();
        updatedIds.setProductIds(dbProductIds);
        return updatedIds;
    }

    public Map<String, Object> removeProductListFromCheckoutSession(String sessionId,
            ListOfProductIdsWrapper productIdsWrapper)
            throws StripeException {
        ListOfProductIdsWrapper updatedIds = removeProductListFromCheckoutSessionCommon(sessionId, productIdsWrapper);
        if (updatedIds.getProductIds().size() < 1) {
            Map<String, Object> json = new HashMap<>();
            json.put("message", "It appears you have removed all checkout items, checkout session is empty");
            json.put("dbProductIds", updatedIds);
            json.put("sessionId", null);
            json.put("checkoutUrl", null);
            return json;
        }
        Session updatedSession = createCheckoutSession(updatedIds);
        return updatedProductsReturnJsonValue(updatedIds, updatedSession);
    }

    public Map<String, Object> removeProductListFromCheckoutSession(String sessionId,
            ListOfProductIdsWrapper productIdsWrapper, Principal principal)
            throws StripeException {
        ListOfProductIdsWrapper updatedIds = removeProductListFromCheckoutSessionCommon(sessionId, productIdsWrapper);
        if (updatedIds.getProductIds().size() < 1) {
            Map<String, Object> json = new HashMap<>();
            json.put("message", "It appears you have removed all checkout items, checkout session is empty");
            json.put("dbProductIds", updatedIds);
            json.put("sessionId", null);
            json.put("checkoutUrl", null);
            return json;
        }
        Session updatedSession = createCheckoutSession(principal, updatedIds);
        return updatedProductsReturnJsonValue(updatedIds, updatedSession);
    }

    private ListOfProductIdsWrapper removeProductFromCheckoutSessionCommon(String sessionId, Long dbProductId)
            throws StripeException {
        Stripe.apiKey = stripeApiKey;
        ProductIdsObjectDTO productIdsObject = getCheckoutSessionLineItems(sessionId, true);
        List<Long> dbProductIds = productIdsObject.getDbProductIds();
        dbProductIds.remove(
                dbProductIds.indexOf(dbProductId));
        ListOfProductIdsWrapper updatedIds = new ListOfProductIdsWrapper();
        updatedIds.setProductIds(dbProductIds);
        return updatedIds;
    }

    public Map<String, Object> removeProductFromCheckoutSession(String sessionId, Long dbProductId)
            throws StripeException {
        ListOfProductIdsWrapper updatedIds = removeProductFromCheckoutSessionCommon(sessionId, dbProductId);
        Session updatedSession = createCheckoutSession(updatedIds);

        return updatedProductsReturnJsonValue(updatedIds, updatedSession);
    }

    public Map<String, Object> removeProductFromCheckoutSession(String sessionId, Long dbProductId, Principal principal)
            throws StripeException {
        ListOfProductIdsWrapper updatedIds = removeProductFromCheckoutSessionCommon(sessionId, dbProductId);
        Session updatedSession = createCheckoutSession(principal, updatedIds);

        return updatedProductsReturnJsonValue(updatedIds, updatedSession);
    }

    //////////////////////////////////////
    // ADD PRODUCTS TO CHECKOUT SESSION
    //////////////////////////////////////
    private ListOfProductIdsWrapper addProductsToCheckoutSessionCommon(String sessionId,
            ListOfProductIdsWrapper productIdsWrapper) throws StripeException {
        Stripe.apiKey = stripeApiKey;
        ProductIdsObjectDTO productIdsObject = getCheckoutSessionLineItems(sessionId, true);
        List<Long> dbProductIds = productIdsObject.getDbProductIds();
        dbProductIds.addAll(productIdsWrapper.getProductIds());
        ListOfProductIdsWrapper updatedIds = new ListOfProductIdsWrapper();
        updatedIds.setProductIds(dbProductIds);
        return updatedIds;
    }

    public Map<String, Object> addProductsToCheckoutSession(String sessionId, ListOfProductIdsWrapper productIdsWrapper)
            throws StripeException {
        ListOfProductIdsWrapper updatedIds = addProductsToCheckoutSessionCommon(sessionId, productIdsWrapper);
        Session updatedSession = createCheckoutSession(updatedIds);

        return updatedProductsReturnJsonValue(updatedIds, updatedSession);
    }

    public Map<String, Object> addProductsToCheckoutSession(String sessionId, ListOfProductIdsWrapper productIdsWrapper,
            Principal principal)
            throws StripeException {
        ListOfProductIdsWrapper updatedIds = addProductsToCheckoutSessionCommon(sessionId, productIdsWrapper);
        Session updatedSession = createCheckoutSession(principal, updatedIds);

        return updatedProductsReturnJsonValue(updatedIds, updatedSession);
    }

    private Map<String, Object> updatedProductsReturnJsonValue(ListOfProductIdsWrapper updatedIds,
            Session updatedSession) {
        Map<String, Object> json = new HashMap<>();
        json.put("dbProductIds", updatedIds);
        json.put("sessionId", updatedSession.getId());
        json.put("checkoutUrl", updatedSession.getUrl());
        return json;
    }

    /////////////////////////////
    // GET CHECKOUT SESSION
    /////////////////////////////
    public Session getCheckoutSession(String sessionId) throws StripeException {
        Stripe.apiKey = stripeApiKey;
        return Session.retrieve(sessionId);
    }

    /////////////////////////////////////
    // GET CHECKOUT SESSION LINE ITEMS
    /////////////////////////////////////
    public Map<String, Object> getCheckoutSessionLineItems(String sessionId) throws StripeException {
        Stripe.apiKey = stripeApiKey;
        Session session = getCheckoutSession(sessionId);
        Map<String, Object> json = new HashMap<>();
        json.put("lineItems", session.listLineItems().getData());
        json.put("checkoutUrl", session.getUrl());
        return json;
    }

    public ProductIdsObjectDTO getCheckoutSessionLineItems(String sessionId, Boolean idsOnly)
            throws StripeException {
        Stripe.apiKey = stripeApiKey;
        Session session = getCheckoutSession(sessionId);
        List<Long> productIds = session.listLineItems().getData()
                .stream()
                .map((LineItem li) -> {
                    try {
                        String productId = li.getPrice().getProduct();
                        Long quantity = li.getQuantity();
                        com.stripe.model.Product product = com.stripe.model.Product.retrieve(productId);
                        String dbProductId = product.getMetadata().get("productId"); // get PG DB product ID

                        System.out.print("\n\n");
                        System.out.print(dbProductId + "\n");
                        System.out.print(product.getMetadata() + "\n");
                        System.out.print("\n\n");

                        List<String> dbProductIds = new ArrayList<>();
                        for (var i = 0; i < quantity; ++i) {
                            dbProductIds.add(dbProductId);
                        }

                        return dbProductIds;
                    } catch (StripeException ex) {
                        System.out.print("\n\n");
                        System.out.print(ex);
                        System.out.print("\n\n");
                        return null;
                    }
                })
                .flatMap(List::stream)
                .map((String id) -> Long.parseLong(id))
                .collect(Collectors.toList());

        return ProductIdsObjectDTO.builder()
                .dbProductIds(productIds)
                .checkoutUrl(session.getUrl())
                .build();
    }

    public Map<String, Object> getCheckoutSessionLineItems(String sessionId,
            String productObjects)
            throws StripeException {
        Stripe.apiKey = stripeApiKey;
        Session session = getCheckoutSession(sessionId);
        if (productObjects.equals("stripe")) {
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
            Map<String, Object> json = new HashMap<>();
            json.put("stripeProducts", products);
            json.put("checkoutUrl", session.getUrl());
            return json;
        } else {
            List<Product> products = session.listLineItems().getData()
                    .stream()
                    .map((li) -> {
                        try {
                            com.stripe.model.Product product = com.stripe.model.Product
                                    .retrieve(li.getPrice().getProduct());
                            String id = product.getMetadata().get("productId");
                            return productsRepo.findById(Long.parseLong(id)).orElseThrow();
                        } catch (StripeException ex) {
                            return null;
                        }
                    })
                    .collect(Collectors.toList());
            Map<String, Object> json = new HashMap<>();
            json.put("dbProducts", products);
            json.put("checkoutUrl", session.getUrl());
            return json;
        }
    }

}

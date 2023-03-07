package com.posas.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.Product;

@Service
public class StripeProductService {

    @Value("${stripe.secret-key}")
    String secretKey;

    public Price createPrice() throws StripeException {
        Stripe.apiKey = secretKey;
        Product product = createProduct();

        Map<String, Object> params = new HashMap<>();
        params.put("unit_amount", 2000);
        params.put("currency", "usd");
        params.put("product", product.getId());

        Price price = Price.create(params);
        return price;
    }

    public Product createProduct() throws StripeException {
        Stripe.apiKey = secretKey;

        Map<String, Object> params = new HashMap<>();
        params.put("name", "Product");

        Product product = Product.create(params);
        return product;
    }

}

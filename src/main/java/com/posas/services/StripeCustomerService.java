package com.posas.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.posas.repositories.ProfileRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;

@Service
public class StripeCustomerService {

    @Value("${stripe.secret-key}")
    String secretKey;

    @Autowired
    ProfileRepository profileRepo;

    public Customer getStripeCustomerByEmail(String email) throws StripeException {
        return Customer.retrieve(
                profileRepo.findByEmail(email).getStripeCustomerId());
    }

    public Customer createStripeCustomer(String name, String email) throws StripeException {
        Stripe.apiKey = secretKey;
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("email", email);

        Customer customer = Customer.create(params);
        return customer;
    }

}

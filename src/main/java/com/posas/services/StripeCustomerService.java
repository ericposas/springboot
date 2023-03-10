package com.posas.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.posas.entities.Profile;
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

    public Customer createStripeCustomer(Profile profile) throws StripeException {
        Stripe.apiKey = secretKey;
        Map<String, Object> params = new HashMap<>();
        params.put("name", profile.getFirstname() + " " + profile.getLastname());
        params.put("email", profile.getEmail());

        // TODO: Why isn't customer name populating to the checkout stage?
        Customer customer = Customer.create(params);
        return customer;
    }

}

package com.posas.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ecomm/checkout")
public class CustomCheckoutController {
    
    // TODO: Build custom checkout flow
    // - Customer adds relevant info like name, email
    // - Customer fills up a Cart (needs to be created) object with db Products
    // - Customer goes to custom checkout endpoint, where the Cart products are returned
    // - Customer can go back to the store and add or remove items or remain in Checkout 
    // - Customer adds payment method(s)
    // - Customer send a request to charge endpoint with chosen paymentMethodID

}

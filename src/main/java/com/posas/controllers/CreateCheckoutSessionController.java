package com.posas.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.posas.services.StripeCustomerService;
import com.posas.services.StripeSessionService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import lombok.Data;

@Data
class CheckoutDTO {
    String email;
}

@RestController
@RequestMapping("/checkout")
public class CreateCheckoutSessionController {

    @Value("${spring.profiles.active}")
    String activeProfile;

    @Value("${stripe.secret-key}")
    String secretKey;

    @Autowired
    StripeCustomerService customerService;

    @Autowired
    StripeSessionService sessionService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> successfulCheckout() {
        return ResponseEntity.ok("{ \"success\": true }");
    }

    @PostMapping
    public ResponseEntity<?> createCheckoutSession(@RequestBody() CheckoutDTO body) throws StripeException {
        Stripe.apiKey = secretKey;
        Customer stripeCustomer = customerService.getStripeCustomerByEmail(body.getEmail());
        String custId = stripeCustomer.getId();
        Session session = sessionService.createStripeCheckout();
        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.SETUP)
                .setCustomer(custId)
                .setSuccessUrl(
                        activeProfile == "dev" ? "http://localhost/checkout/success?session_id=" + session.getId()
                                : "https://webcommerce.live/checkout/success?session_id=" + session.getId())
                .setCancelUrl("https://example.com/cancel")
                .build();

        Session _session = Session.create(params);
        // 303 redirect to session.getUrl()
        return ResponseEntity.ok(_session.toJson());

    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<?> retrieveCheckoutSession(@PathVariable(value = "sessionId") String sessionId)
            throws StripeException {
        Stripe.apiKey = secretKey;
        Session session = Session.retrieve(sessionId);
        return ResponseEntity.ok(session.toJson());
    }

}

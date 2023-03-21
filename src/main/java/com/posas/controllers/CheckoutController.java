package com.posas.controllers;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.posas.dtos.ListOfProductIdsWrapper;
import com.posas.services.CheckoutSessionService;
import com.stripe.exception.StripeException;

@RestController
@RequestMapping("/checkout")
public class CheckoutController {

    @Autowired
    CheckoutSessionService checkoutSessionService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createCheckoutSessionCustomer(Principal principal,
            @RequestBody ListOfProductIdsWrapper productIds)
            throws StripeException {
        if (!SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.ok(checkoutSessionService.createCheckoutSession(principal, productIds).toJson());
        }
        return ResponseEntity.ok(checkoutSessionService.createCheckoutSession(productIds).toJson());
    }

    @GetMapping(path = "/success", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> successPage() {
        return ResponseEntity.ok("{ \"checkout\": \"success\" }");
    }

    @GetMapping(path = "/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> cancelPage() {
        return ResponseEntity.ok("{ \"checkout\": \"canceled.\" }");
    }

    @DeleteMapping("/sessions/{sessionId}/{dbProductId}")
    public ResponseEntity<?> deleteItemAndReturnNewSession(
            Principal principal,
            @PathVariable("sessionId") String sessionId,
            @PathVariable("dbProductId") Long dbProductId)
            throws StripeException {
        if (!SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.ok(
                    checkoutSessionService.removeProductFromCheckoutSession(sessionId, dbProductId, principal));
        } else {
            return ResponseEntity.ok(
                    checkoutSessionService.removeProductFromCheckoutSession(sessionId, dbProductId));

        }
    }

    /////////////////////////////////////////////
    // POST Mapping to DELETE Multiple Products
    /////////////////////////////////////////////
    @PostMapping("/sessions/{sessionId}/delete")
    public ResponseEntity<?> deleteListOfItemsAndReturnNewSession(
            Principal principal,
            @PathVariable("sessionId") String sessionId,
            @RequestBody ListOfProductIdsWrapper productIdsDTO)
            throws StripeException {
        if (!SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.ok(
                    checkoutSessionService.removeProductListFromCheckoutSession(sessionId, productIdsDTO, principal));
        } else {
            return ResponseEntity.ok(
                    checkoutSessionService.removeProductListFromCheckoutSession(sessionId, productIdsDTO));

        }
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<String> retrieveCheckoutSessionById(
            @PathVariable("sessionId") String sessionId)
            throws StripeException {

        System.out.print("\n\n");
        System.out.print(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        System.out.print("\n\n");

        return ResponseEntity.ok(
                checkoutSessionService.getCheckoutSession(sessionId).toJson());
    }

    @PutMapping("/sessions/{sessionId}")
    public ResponseEntity<?> addItemsAndReturnNewSession(
            Principal principal,
            @PathVariable("sessionId") String sessionId,
            @RequestBody ListOfProductIdsWrapper productIdsDTO)
            throws StripeException {
        if (!SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.ok(
                    checkoutSessionService.addProductsToCheckoutSession(sessionId, productIdsDTO, principal));
        } else {
            return ResponseEntity.ok(
                    checkoutSessionService.addProductsToCheckoutSession(sessionId, productIdsDTO));
        }
    }

    @GetMapping(path = "/sessions/{sessionId}/lineitems")
    public ResponseEntity<?> retrieveCheckoutSessionLineItems(
            @PathVariable("sessionId") String sessionId,
            @RequestParam(value = "idsOnly", required = false) Boolean idsOnly,
            @RequestParam(value = "productObjects", required = false) String productObjects)
            throws StripeException {
        if (idsOnly != null && idsOnly == true) {
            return ResponseEntity.ok(
                    checkoutSessionService.getCheckoutSessionLineItems(sessionId, true));
        }
        if (productObjects != null) {
            return ResponseEntity.ok(
                    checkoutSessionService.getCheckoutSessionLineItems(sessionId, productObjects));
        }
        return ResponseEntity.ok(
                checkoutSessionService.getCheckoutSessionLineItems(sessionId));
    }

}

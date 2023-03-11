package com.posas.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.posas.dtos.ProductCreationResponseDTO;
import com.posas.dtos.ProductDTO;
import com.posas.services.ProductsService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Product;
import com.stripe.param.ProductListParams;

@RestController
@RequestMapping("/products")
public class ProductsController {

    @Value("${stripe.secret-key}")
    private String stripeApiKey;

    @Autowired
    ProductsService productsService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createNewProduct(@RequestBody ProductDTO productDTO)
            throws StripeException {
        ProductCreationResponseDTO created = productsService.createProduct(productDTO);
        return ResponseEntity.ok(created);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listProducts(
            @RequestParam(value = "productType", required = false) String from,
            @RequestParam(value = "idsOnly", required = false) Boolean dbIdsOnly)
            throws StripeException {
        Stripe.apiKey = stripeApiKey;
        if (from != null && from.equals("stripe")) {
            return ResponseEntity.ok(
                    Product.list(ProductListParams
                            .builder()
                            .setActive(true)
                            .build()).toJson());
        }
        if (dbIdsOnly != null) {
            return ResponseEntity.ok(
                    productsService.listAllStoreDBProductsIdsOnly());
        }
        return ResponseEntity.ok(
                productsService.listAllStoreDBProducts());
    }

    @DeleteMapping(path = "/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteProductById(@PathVariable("productId") Long productId) throws StripeException {
        return ResponseEntity.ok(this.productsService.deleteArchiveProduct(productId));
    }

    @DeleteMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteListOfProductsById(@RequestBody Map<String, List<Long>> idsToDelete)
            throws StripeException {
        return ResponseEntity.ok(this.productsService.deleteListOfProducts(idsToDelete.get("productIds")));
    }

}

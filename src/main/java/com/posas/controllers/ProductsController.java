package com.posas.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.posas.dtos.ProductCreationResponseDTO;
import com.posas.dtos.ProductDTO;
import com.posas.services.StripeProductsPricesService;
import com.stripe.exception.StripeException;

@RestController
@RequestMapping("/products")
public class ProductsController {

    @Autowired
    StripeProductsPricesService productsService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createNewProduct(@RequestBody ProductDTO productDTO)
            throws StripeException {
        ProductCreationResponseDTO created = productsService.createProduct(productDTO);
        return ResponseEntity.ok(created);
    }

}

package com.posas.services;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.posas.dtos.ProductCreationResponseDTO;
import com.posas.dtos.ProductDTO;
import com.posas.dtos.ProductDeletionResponseDTO;
import com.posas.dtos.UnsplashImageDTO;
import com.posas.dtos.UnsplashSearchParams;
import com.posas.http.HttpHelpers;
import com.posas.repositories.ProductRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Product;
import com.stripe.param.ProductCreateParams;
import com.stripe.param.ProductCreateParams.DefaultPriceData;
import com.stripe.param.ProductUpdateParams;

@Service
public class StripeProductsPricesService {

    @Autowired
    @Qualifier("getActiveProfile")
    String activeProfile;

    @Autowired
    @Qualifier("getStripeApiKey")
    String stripeApiKey;

    @Autowired
    @Qualifier("getUnsplashApiKey")
    String unsplashKey;

    @Autowired
    ProductRepository productRepo;

    public String fetchImageFromUnsplash(UnsplashSearchParams searchUnsplash) {
        String imageUrl = "https://api.unsplash.com/photos/random?query=" + searchUnsplash.getProductImageOf()
                + "&collections=" + searchUnsplash.getUnsplashCollectionsVar()
                + "&client_id=" + unsplashKey;
        try {
            var request = HttpHelpers.httpGET(imageUrl);
            var response = HttpHelpers.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            ObjectMapper mapper = new ObjectMapper();
            var imageResult = mapper.readValue(response.body(), UnsplashImageDTO.class)
                    .getUrls()
                    .getRegular();
            return imageResult;
        } catch (IOException | InterruptedException ex) {
            System.out.print(ex);
        }
        return null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public ProductCreationResponseDTO createProduct(ProductDTO productDTO) throws StripeException {
        Stripe.apiKey = stripeApiKey;
        String imageResult = "";
        if (productDTO.getUnsplashSearchParams() != null) {
            imageResult = fetchImageFromUnsplash(productDTO.getUnsplashSearchParams());
        } else {
            imageResult = productDTO.getProvidedImageUrl();
        }
        // save to Stripe dashboard
        Product product = Product.create(
                ProductCreateParams.builder()
                        .setActive(true)
                        .setDefaultPriceData(DefaultPriceData.builder()
                                .setCurrency("USD")
                                .setUnitAmount(productDTO.getProductPrice())
                                .build())
                        .setName(productDTO.getProductName())
                        .setDescription(productDTO.getProductDescription())
                        .addImage(imageResult)
                        .build());

        try {
            com.posas.entities.Product storeProduct = new com.posas.entities.Product();
            // now save the product to our own database
            if (productRepo.findByName(productDTO.getProductName()) != null) {
                throw new SQLException("Duplicate key: Product \"name\"");
            }
            storeProduct.setName(productDTO.getProductName());
            storeProduct.setDescription(productDTO.getProductDescription());
            storeProduct.setPrice(productDTO.getProductPrice());
            storeProduct.setImageUrl(imageResult);
            storeProduct.setDeleted(false);
            storeProduct.setStripeProductId(product.getId());
            storeProduct.setPageUrl(getUrlBase() + product.getId());
            productRepo.save(storeProduct);

            return ProductCreationResponseDTO.builder()
                    .storeProduct(storeProduct)
                    .stripeProduct(product.toJson())
                    .build();
        } catch (SQLException ex) {
            var errMsg = "Duplicate product name: Skipping database save; Archiving assoc. Stripe product;";
            System.out.print("\n\n");
            System.out.print(errMsg);
            System.out.print("\n\n");
            Product retrieved = Product.retrieve(product.getId());
            retrieved.update(ProductUpdateParams.builder()
                    .setActive(false)
                    .build());
            com.posas.entities.Product dbProduct = productRepo.findByName(productDTO.getProductName());
            dbProduct.setDeleted(true);
            productRepo.save(dbProduct);

            return ProductCreationResponseDTO.builder()
                    .message(errMsg)
                    .build();
        }
    }

    public ProductDeletionResponseDTO deleteArchiveProduct(Long productId) throws StripeException {
        Stripe.apiKey = stripeApiKey;

        com.posas.entities.Product product = productRepo.findById(productId)
                .orElseThrow();
        product.setDeleted(true);
        productRepo.save(product);

        com.stripe.model.Product stripeProduct = Product.retrieve(product.getStripeProductId());
        String updatedStripeProduct = stripeProduct.update(ProductUpdateParams.builder()
                .setActive(false)
                .build()).toJson();

        return ProductDeletionResponseDTO.builder()
                .storeProduct(productRepo.findById(productId).orElseThrow())
                .stripeProduct(updatedStripeProduct)
                .message("")
                .build();
    }

    private String getUrlBase() {
        if (activeProfile.trim().equals("dev")) {
            return "http://localhost/api/products/";
        } else {
            return "https://webcommerce.live/api/products/";
        }
    }

    public List<com.posas.entities.Product> listAllStoreDBProducts() {
        return productRepo.findAllNonDeleted();
    }

}

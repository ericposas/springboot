package com.posas.services;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.posas.dtos.ProductCreationResponseDTO;
import com.posas.dtos.ProductDTO;
import com.posas.dtos.UnsplashSearchParams;
import com.posas.http.HttpHelpers;
import com.posas.repositories.ProductRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Product;
import com.stripe.param.ProductCreateParams;
import com.stripe.param.ProductCreateParams.DefaultPriceData;

import lombok.Data;

@Data
class UnsplashImageURLs {
    String raw;
    String full;
    String regular;
    String small;
    String thumb;
    String small_s3;
}

@Data
class UnsplashImageDTO {
    String id;
    String created_at;
    String updated_at;
    String promoted_at;
    Long width;
    Long height;
    String color;
    String blur_hash;
    String description;
    String alt_description;
    UnsplashImageURLs urls;
    Object links;
    Long likes;
    Boolean liked_by_user;
    Object current_user_collections;
    Boolean sponsorship;
    Object topic_submissions;
    Object user;
    Object exif;
    Object location;
    Object meta;
    Boolean public_domain;
    List<Object> tags;
    List<Object> tags_preview;
    Long views;
    String downloads;
    Object topics;
}

@Service
public class StripeProductsPricesService {

    @Value("${spring.profiles.active}")
    String activeProfile;

    @Value("${stripe.secret-key}")
    String secretKey;

    @Value("${unsplash.access-key}")
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
        Stripe.apiKey = secretKey;
        String imageResult = "";
        if (productDTO.getUnsplashSearchParams() != null) {
            imageResult = fetchImageFromUnsplash(productDTO.getUnsplashSearchParams());
        } else {
            imageResult = productDTO.getProvidedImageUrl();
        }
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
        // TODO: Handle duplicates
        // Error in Stripe saying, "This product cannot be deleted because it has one or
        // more user-created Prices"
        com.posas.entities.Product storeProduct = new com.posas.entities.Product();
        // now save the product to our own database
        storeProduct.setName(productDTO.getProductName());
        storeProduct.setDescription(productDTO.getProductDescription());
        storeProduct.setPrice(productDTO.getProductPrice());
        storeProduct.setImageUrl(imageResult);
        storeProduct.setStripeProductId(product.getId());
        storeProduct.setPageUrl(getUrlBase() + product.getId());
        productRepo.save(storeProduct);

        return ProductCreationResponseDTO.builder()
                .storeProduct(storeProduct)
                .stripeProduct(product.toJson())
                .build();
    }

    private String getUrlBase() {
        if (activeProfile.trim().equals("dev")) {
            return "http://localhost/api/products/";
        } else {
            return "https://webcommerce.live/api/products/";
        }
    }

}

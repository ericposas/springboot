package com.posas.services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.posas.dtos.ProductDTO;
import com.posas.dtos.UnsplashSearchParams;
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

    @Value("${stripe.secret-key}")
    String secretKey;

    @Value("${unsplash.access-key}")
    String unsplashKey;

    public HttpClient getHttpClient() {
        var client = HttpClient.newHttpClient();
        return client;
    }

    public HttpRequest buildRequest(String url) {
        return HttpRequest
                .newBuilder()
                .uri(URI.create(url))
                .header("accept", "application/json")
                .GET()
                .build();
    }

    public String fetchImageFromUnsplash(UnsplashSearchParams searchUnsplash) {
        String imageUrl = "https://api.unsplash.com/photos/random?query=" + searchUnsplash.getProductImageOf()
                + "&collections=" + searchUnsplash.getUnsplashCollectionsVar()
                + "&client_id=" + unsplashKey;
        try {
            var request = buildRequest(imageUrl);
            var response = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
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
    public Product createProduct(ProductDTO productDTO) throws StripeException {
        Stripe.apiKey = secretKey;
        String imageResult = "";
        if (productDTO.getUnsplashSearchParams()  != null) {
            imageResult = fetchImageFromUnsplash(productDTO.getUnsplashSearchParams());
        } else {
            imageResult = productDTO.getProvidedImageUrl();
        }
        Product product = Product.create(
                ProductCreateParams.builder()
                        .setDefaultPriceData(DefaultPriceData.builder()
                                .setCurrency("USD")
                                .setUnitAmount(productDTO.getProductPrice())
                                .build())
                        .setName(productDTO.getProductName())
                        .addImage(imageResult)
                        .setActive(true)
                        .setDescription(productDTO.getProductDescription())
                        .setUrl("http://localhost/api/products/" + productDTO.getProductName())
                        .build());
        return product;
    }

}

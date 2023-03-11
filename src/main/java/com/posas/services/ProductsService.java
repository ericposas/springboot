package com.posas.services;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

@Service("ProductsService")
public class ProductsService {

    @Value("${stripe.secret-key}")
    private String stripeApiKey;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Value("${unsplash.access-key}")
    private String unsplashKey;

    @Autowired
    ProductRepository productRepo;

    public String fetchImageFromUnsplash(UnsplashSearchParams searchUnsplash) {
        String imageUrl = "https://api.unsplash.com/photos/random?query=" + searchUnsplash.getProductImageOf()
                + "&collections=" + searchUnsplash.getUnsplashCollections()
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

    public ProductCreationResponseDTO createProduct(ProductDTO productDTO) throws StripeException {
        Stripe.apiKey = stripeApiKey;
        String imageResult = "";
        if (productDTO.getUnsplashSearchParams() != null) {
            imageResult = fetchImageFromUnsplash(productDTO.getUnsplashSearchParams());
        } else {
            imageResult = productDTO.getProvidedImageUrl();
        }
        com.posas.entities.Product storeProductQuery = productRepo.findByName(productDTO.getProductName());
        if (storeProductQuery == null ||
                (storeProductQuery != null && storeProductQuery.getDeleted() != false)) {
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
            // now save the product to our own database
            com.posas.entities.Product storeProduct = new com.posas.entities.Product();
            storeProduct.setName(productDTO.getProductName());
            storeProduct.setDescription(productDTO.getProductDescription());
            storeProduct.setPrice(productDTO.getProductPrice());
            storeProduct.setImageUrl(imageResult);
            storeProduct.setDeleted(false);
            storeProduct.setStripeProductId(product.getId());
            storeProduct.setPageUrl((activeProfile.equals("dev") ? "http://localhost/api/products/"
                    : "https://webcommerce.live/api/products/") + product.getId());
            productRepo.save(storeProduct);

            com.posas.entities.Product retrieved = productRepo.findByName(product.getName());
            Map<String, Long> metadata = new HashMap<>();
            metadata.put("db_product_id", retrieved.getProductId());
            Map<String, Object> updateParams = new HashMap<>();
            updateParams.put("metadata", metadata);
            Product updated = product.update(updateParams);

            return ProductCreationResponseDTO.builder()
                    .storeProduct(storeProduct)
                    .stripeProduct(updated.toJson())
                    .build();
        }

        var errMsg = "Duplicate product name: Skipping database save;";
        return ProductCreationResponseDTO.builder()
                .message(errMsg)
                .build();

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
                .message("Archived Stripe product; Deleted DB product")
                .build();
    }

    public String deleteListOfProducts(List<Long> productIds) throws StripeException {
        productIds.stream()
                .forEach((id) -> {
                    // soft-delete db product
                    com.posas.entities.Product dbProduct = productRepo.findById(id).orElseThrow();
                    dbProduct.setDeleted(true);
                    String stripeProductId = dbProduct.getStripeProductId();
                    productRepo.save(dbProduct);
                    // update Stripe, archive product(s)
                    try {
                        Product stripeProduct = Product.retrieve(stripeProductId);
                        stripeProduct.update(ProductUpdateParams.builder()
                                .setActive(false)
                                .build());
                    } catch (StripeException ex) {
                        System.out.print(ex);
                    }
                });
        return "{ \"success\": \"" + productIds.size() + " products deleted.\" }";
    }

    public Map<String, List<com.posas.entities.Product>> listAllStoreDBProducts() {
        List<com.posas.entities.Product> products = productRepo.findAllNonDeleted();
        Map<String, List<com.posas.entities.Product>> map = new HashMap<>();
        map.put("dbProducts", products);
        return map;
    }

    public Map<String, List<Long>> listAllStoreDBProductsIdsOnly() {
        List<Long> productIds = productRepo.findAllNonDeleted().stream()
                .map((product) -> product.getProductId())
                .collect(Collectors.toList());
        Map<String, List<Long>> map = new HashMap<>();
        map.put("dbProductIds", productIds);
        return map;
    }

    public com.posas.entities.Product getDbProduct(Long productId) {
        return productRepo.findById(productId).orElseThrow();
    }

}

package com.posas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Values {

    @Value("${stripe.secret-key}")
    private String stripeApiKey;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Value("${unsplash.access-key}")
    private String unsplashKey;

    @Bean
    public String getStripeApiKey() {
        return this.stripeApiKey;
    }

    @Bean
    public String getUnsplashApiKey() {
        return this.unsplashKey;
    }

    @Bean
    public String getActiveProfile() {
        return this.activeProfile;
    }

}

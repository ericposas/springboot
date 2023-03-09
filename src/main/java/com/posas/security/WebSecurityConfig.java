package com.posas.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    public static final String ADMIN = "admin";
    public static final String USER = "user";
    private final JwtAuthConverter jwtAuthConverter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests()
                // using permitAll() on a particular requestMatchers() allows us to check access
                // control in the Controller(s) themselves
                .requestMatchers(HttpMethod.GET, "/checkout/success").permitAll()
                .requestMatchers(HttpMethod.GET, "/secured", "/secured/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/test", "/test/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/admin", "/admin/**").hasRole(ADMIN)
                .requestMatchers(HttpMethod.POST, "/admin", "/admin/**").hasRole(ADMIN)
                .requestMatchers(HttpMethod.GET, "/profiles", "/profiles/**").hasRole(ADMIN)
                .requestMatchers(HttpMethod.POST, "/profiles", "/profiles/**").hasAnyRole(USER, ADMIN)
                .requestMatchers(HttpMethod.GET, "/user", "/user/**").hasAnyRole(ADMIN, USER)
                .requestMatchers(HttpMethod.POST, "/user", "/user/**").hasAnyRole(ADMIN, USER)
                .requestMatchers(HttpMethod.GET, "/charge", "/charge/**").hasAnyRole(ADMIN, USER)
                .requestMatchers(HttpMethod.POST, "/charge", "/charge/**").hasAnyRole(ADMIN, USER)
                .requestMatchers(HttpMethod.GET, "/products", "/products/**").hasAnyRole(ADMIN, USER)
                .requestMatchers(HttpMethod.POST, "/products", "/products/**").hasAnyRole(ADMIN, USER)
                .requestMatchers(HttpMethod.DELETE, "/products", "/products/**").hasAnyRole(ADMIN, USER)
                .requestMatchers(HttpMethod.GET, "/checkout", "/checkout/**").hasAnyRole(ADMIN, USER)
                .requestMatchers(HttpMethod.POST, "/checkout", "/checkout/**").hasAnyRole(ADMIN, USER)
                .requestMatchers(HttpMethod.GET, "/paymentmethod", "/paymentmethod/**").hasAnyRole(ADMIN, USER)
                .requestMatchers(HttpMethod.POST, "/paymentmethod", "/paymentmethod/**").hasAnyRole(ADMIN, USER);
        http.oauth2ResourceServer()
                .jwt()
                .jwtAuthenticationConverter(jwtAuthConverter);
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        return http.build();
    }

}
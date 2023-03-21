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
        http.csrf().disable();
        http.authorizeHttpRequests()
                // using permitAll() on a particular requestMatchers() allows us to check access
                // control in the Controller(s) themselves
                // Test routes
                .requestMatchers(HttpMethod.GET, "/secured", "/secured/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/test", "/test/**").permitAll()
                // Products
                .requestMatchers(HttpMethod.GET, "/products", "/products/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/products", "/products/**").hasAnyRole(ADMIN)
                .requestMatchers(HttpMethod.DELETE, "/products", "/products/**").hasAnyRole(ADMIN)
                .requestMatchers(HttpMethod.DELETE, "/products/list", "/products/list/**").hasAnyRole(ADMIN)
                // Checkout
                .requestMatchers(HttpMethod.GET, "/checkout", "/checkout/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/checkout", "/checkout/**").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/checkout", "/checkout/**").permitAll()
                .requestMatchers(HttpMethod.PUT, "/checkout", "/checkout/**").permitAll()
                // Profiles
                .requestMatchers(HttpMethod.GET, "/profiles", "/profiles/**").hasRole(ADMIN)
                // User routes need condition (User is Self),
                // i.e. only allow updates if the user is updating their own profile
                .requestMatchers(HttpMethod.GET, "/user", "/user/**").hasRole(USER)
                .requestMatchers(HttpMethod.POST, "/user", "/user/**").hasRole(USER)
                // Charge
                .requestMatchers(HttpMethod.GET, "/charge", "/charge/**").hasAnyRole(ADMIN, USER)
                .requestMatchers(HttpMethod.POST, "/charge", "/charge/**").hasAnyRole(ADMIN, USER)
                .requestMatchers(HttpMethod.GET, "/payments/methods", "/payments/methods/**").hasAnyRole(ADMIN, USER)
                .requestMatchers(HttpMethod.POST, "/payments/methods", "/payments/methods/**").hasAnyRole(ADMIN, USER);
        http.oauth2ResourceServer()
                .jwt()
                .jwtAuthenticationConverter(jwtAuthConverter);
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        return http.build();
    }

}
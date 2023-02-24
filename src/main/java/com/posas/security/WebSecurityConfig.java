package com.posas.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    public static final String ADMIN = "admin";
    public static final String USER = "user";
    public static final String[] AVAILABLE_SCOPES = {
            "SCOPE_test:create",
            "SCOPE_test:view"
    };
    private final JwtAuthConverter jwtAuthConverter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests()
                .requestMatchers(HttpMethod.GET, "/test/anonymous", "/test/anonymous/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/admin", "/admin/**").hasRole(ADMIN)
                .requestMatchers(HttpMethod.GET, "/user", "/user/**").hasAnyRole(ADMIN, USER)
                .requestMatchers(HttpMethod.GET, "/test/scope").hasAnyAuthority(AVAILABLE_SCOPES)
                .requestMatchers(HttpMethod.GET, "/test", "/test/**").hasAnyAuthority("SCOPE_test:view")
                .requestMatchers(HttpMethod.POST, "/test", "/test/**").hasAnyAuthority("SCOPE_test:create")
                .anyRequest().authenticated();
        http.oauth2ResourceServer()
                .jwt()
                .jwtAuthenticationConverter(jwtAuthConverter);
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        return http.build();
    }

}
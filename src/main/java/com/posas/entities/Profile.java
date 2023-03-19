package com.posas.entities;

import java.sql.Time;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity(name = "Profile")
@Table(name = "PROFILES")
public class Profile {
    @Id
    @Column(name = "profile_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long profileId;
    private String preferredUsername;
    private String firstname;
    private String lastname;
    private String email;
    private String phone;
    private String stripeCustomerId;
    private Set<String> stripePaymentMethodIds;
    private String latestCheckoutSession;
    // TODO: Change these to DATE objects / strings
    private Time createdAt;
    private Time updatedAt;
    private Time deletedAt;

    @JsonManagedReference
    @OneToOne(mappedBy = "profile")
    private Address address;

    @JsonManagedReference
    @OneToOne(mappedBy = "profile")
    private Shipping shipping;
}

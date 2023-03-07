package com.posas.entities;

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
    Long profileId;
    String preferredUsername;
    String firstname;
    String lastname;
    String email;
    String stripeCustomerId;
    Set<String> stripePaymentMethodIds;

    @JsonManagedReference
    @OneToOne(optional = false, mappedBy = "profile")
    Address address;
}

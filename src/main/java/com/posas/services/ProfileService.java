package com.posas.services;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.posas.dtos.AddressDTO;
import com.posas.dtos.ProfileDTO;
import com.posas.entities.Address;
import com.posas.entities.Profile;
import com.posas.helpers.TokenHelpers;
import com.posas.repositories.AddressRepository;
import com.posas.repositories.ProfileRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
class CreateUserProfileResponseDTO {
    Long profileId;
    String message;
}

@Data
@Builder
class ListUserProfileResponseDTO {
    List<Profile> profiles;
    String message;
}

@Data
@Builder
class CreateUserFromJwtAuthResponseDTO {
    Profile profile;
    String message;
}

/**
 * {@summary} JWT Token extracted properties
 */
@Data
@Builder
class JwtProfileDataDTO {
    String sid;
    String iss;
    String name;
    Long authTime;
    Boolean emailVerified;
    List<String> roles;
    List<String> scopes;
    String givenName;
    String familyName;
    String email;
    String preferredUsername;
    Profile profile;
    String message;
}

@Service
public class ProfileService {

    @Autowired
    @Qualifier("clientId")
    private String clientId;

    @Autowired
    ProfileRepository profileRepo;

    @Autowired
    AddressRepository addressRepo;

    @Autowired
    StripeCustomerService customerService;

    public Profile createAddress(AddressDTO body, Principal principal) throws StripeException {
        String email = TokenHelpers.getFromJwt(principal, "email");
        Profile profile = profileRepo.findByEmail(email);
        if (profile == null) {
            profile = (Profile) createProfileFromJwtData(principal); // .get("profile");
        }
        Address address = new Address();
        address.setCity(body.getCity());
        address.setPostalCode(body.getPostalCode());
        address.setState(body.getState());
        address.setStreetname(body.getStreetname());
        address.setStreetnum(body.getStreetnum());
        address.setProfile(profile);
        try {
            addressRepo.save(address);
        } catch (Exception ex) {
            System.out.print(ex);
        }

        profile.setAddress(address);
        profileRepo.save(profile);

        return profileRepo.findByEmail(email);
    }

    public JwtProfileDataDTO getJwtProfileData(Principal principal) throws StripeException {
        List<String> roles = TokenHelpers.getTokenResource(principal)
                .get(clientId)
                .get("roles");
        String scopesStr = (String) TokenHelpers.getTokenAttributes(principal).get("scope");
        List<String> scopes = List.of(scopesStr.split(" "));
        String name = TokenHelpers.getFromJwt(principal, "name");
        String preferredUsername = TokenHelpers.getFromJwt(principal, "preferred_username");
        String email = TokenHelpers.getFromJwt(principal, "email");
        String givenName = TokenHelpers.getFromJwt(principal, "given_name");
        String familyName = TokenHelpers.getFromJwt(principal, "family_name");
        String sid = TokenHelpers.getFromJwt(principal, "sid");
        String iss = TokenHelpers.getFromJwt(principal, "iss");
        Long authTime = (Long) TokenHelpers.getTokenAttributes(principal).get("auth_time");
        Boolean emailVerified = (Boolean) TokenHelpers.getTokenAttributes(principal).get("email_verified");

        Profile profileByEmail = profileRepo.findByEmail(email);
        Profile existingProfile = profileByEmail != null ? profileByEmail
                : (Profile) createProfileFromJwtData(principal); // .get("profile");

        return JwtProfileDataDTO.builder()
                .iss(iss)
                .sid(sid)
                .authTime(authTime)
                .roles(roles)
                .scopes(scopes)
                .name(name)
                .email(email)
                .preferredUsername(preferredUsername)
                .emailVerified(emailVerified)
                .givenName(givenName)
                .familyName(familyName)
                .profile(existingProfile)
                .message("Details about you.")
                .build();
    }

    public ListUserProfileResponseDTO getAllUserProfiles()
            throws RuntimeException {
        List<Profile> result = profileRepo.findAll();
        return ListUserProfileResponseDTO.builder()
                .profiles(result)
                .message("Retrieved list of user profiles")
                .build();
    }

    public Profile createProfileFromJwtData(Principal principal) throws StripeException {
        Profile profile = new Profile();
        String email = TokenHelpers.getFromJwt(principal, "email");
        if (email != null && profileRepo.findByEmail(email) == null) {
            String preferredUsername = TokenHelpers.getFromJwt(principal, "preferred_username");
            String firstname = TokenHelpers.getFromJwt(principal, "given_name");
            String lastname = TokenHelpers.getFromJwt(principal, "family_name");
            if (email != null)
                profile.setEmail(email);
            if (preferredUsername != null)
                profile.setPreferredUsername(preferredUsername);
            if (firstname != null)
                profile.setFirstname(firstname);
            if (lastname != null)
                profile.setLastname(lastname);
            Customer customer = customerService.createStripeCustomer(firstname + lastname, email);
            profile.setStripeCustomerId(customer.getId());
            profileRepo.saveAndFlush(profile);
            return profile;
        }
        return profileRepo.findByEmail(email);
    }

    public CreateUserFromJwtAuthResponseDTO createUserProfileFromJwtAuthDataAndResponse(Principal principal)
            throws StripeException {
        Profile profile = (Profile) createProfileFromJwtData(principal);
        return CreateUserFromJwtAuthResponseDTO.builder()
                .profile(profile)
                .message(profile != null ? "Created profile from authenticated user."
                        : "Returned already existing profile.")
                .build();
    }

    public CreateUserProfileResponseDTO createUserProfileWithOptionalAddress(ProfileDTO profileData,
            Principal principal)
            throws RuntimeException, StripeException {

        Address address = new Address();
        if (profileData.getAddress() != null) {
            address.setStreetnum(profileData.getAddress().getStreetnum());
            address.setStreetname(profileData.getAddress().getStreetname());
            address.setCity(profileData.getAddress().getCity());
            address.setState(profileData.getAddress().getState());
            address.setPostalCode(profileData.getAddress().getPostalCode());
            addressRepo.save(address);
        } else {
            System.out.print("No Address data sent");
        }

        Profile profile = createProfileFromJwtData(principal);
        if (address != null) {
            profile.setAddress(address);
            profileRepo.saveAndFlush(profile);
        }

        return CreateUserProfileResponseDTO.builder()
                .message("Created new user profile")
                .profileId(profile.getProfileId())
                .build();
    }

}

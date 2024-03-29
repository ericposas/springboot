package com.posas.services;

import java.security.Principal;
import java.sql.Time;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.posas.dtos.AddressDTO;
import com.posas.dtos.AttributesDTO;
import com.posas.dtos.CreateUserFromJwtAuthResponseDTO;
import com.posas.entities.Address;
import com.posas.entities.Profile;
import com.posas.entities.Shipping;
import com.posas.helpers.TokenHelpers;
import com.posas.repositories.AddressRepository;
import com.posas.repositories.ProfileRepository;
import com.posas.repositories.ShippingRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.param.CustomerUpdateParams;

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

    @Value("${stripe.secret-key}")
    String stripeApiKey;

    @Autowired
    ProfileRepository profileRepo;

    @Autowired
    AddressRepository addressRepo;

    @Autowired
    ShippingRepository shippingRepo;

    @Autowired
    CustomerService customerService;

    public Profile getProfile(Principal principal) {
        return profileRepo.findByEmail(
                TokenHelpers.getFromJwt(principal, "email"));
    }

    public AttributesDTO getScopes(Principal principal) {
        return AttributesDTO.builder()
                .roles(TokenHelpers.getTokenResource(principal).get(clientId)
                        .get("roles"))
                .scopes(List.of(((String) TokenHelpers.getTokenAttributes(principal)
                        .get("scope")).split(" ")))
                .username(TokenHelpers.getFromJwt(principal, "preferred_username"))
                .message("/api/test/scopes")
                .build();
    }

    public Profile updatePhone(Principal principal, String phone) throws StripeException {
        Stripe.apiKey = stripeApiKey;
        Profile profile = getProfile(principal);
        profile.setPhone(phone);
        profile.setUpdatedAt(new Time(System.currentTimeMillis()));
        Customer customer = Customer.retrieve(profile.getStripeCustomerId());
        customer.update(CustomerUpdateParams.builder()
                .setPhone(phone)
                .build());
        return profileRepo.save(profile);
    }

    public Profile saveAddress(AddressDTO body, Principal principal) throws StripeException {
        Profile profile = getProfile(principal);
        if (profile == null) {
            profile = (Profile) createProfileFromJwtData(principal);
        }

        Address address = profile.getAddress();
        if (profile.getAddress() == null) {
            address = new Address();
            address.setCreatedAt(new Time(System.currentTimeMillis()));
        }
        address.setCity(body.getCity());
        address.setCountry(body.getCountry());
        address.setLine1(body.getLine1());
        address.setLine2(body.getLine2());
        address.setPostalCode(body.getPostalCode());
        address.setState(body.getState());
        address.setType("billing");
        address.setUpdatedAt(new Time(System.currentTimeMillis()));
        address.setProfile(profile);
        addressRepo.save(address);
        profile.setAddress(address);
        profile.setUpdatedAt(new Time(System.currentTimeMillis()));
        profileRepo.save(profile);

        // update Stripe customer data
        Stripe.apiKey = stripeApiKey;
        Map<String, Object> addrParams = new HashMap<>();
        Map<String, Object> custParams = new HashMap<>();
        Customer customer = Customer.retrieve(profile.getStripeCustomerId());
        addrParams.put("city", body.getCity());
        addrParams.put("country", body.getCountry());
        addrParams.put("line1", body.getLine1());
        addrParams.put("line2", body.getLine2());
        addrParams.put("postal_code", body.getPostalCode());
        addrParams.put("state", body.getState());
        custParams.put("address", addrParams);
        customer.update(custParams);

        // if no shipping, save billing as shipping address
        Profile updated = getProfile(principal);
        if (updated.getShipping() == null) {
            updated = saveShippingAddress(body, principal);
        }

        return updated;
    }

    public Profile saveShippingAddress(AddressDTO body, Principal principal) throws StripeException {
        Profile profile = getProfile(principal);
        if (profile == null) {
            profile = (Profile) createProfileFromJwtData(principal);
        }

        Shipping shipping = profile.getShipping();
        if (profile.getShipping() == null) {
            shipping = new Shipping();
            shipping.setCreatedAt(new Time(System.currentTimeMillis()));
        }
        shipping.setCity(body.getCity());
        shipping.setCountry(body.getCountry());
        shipping.setLine1(body.getLine1());
        shipping.setLine2(body.getLine2());
        shipping.setPostalCode(body.getPostalCode());
        shipping.setState(body.getState());
        shipping.setProfile(profile);
        shipping.setType("shipping");
        shipping.setUpdatedAt(new Time(System.currentTimeMillis()));
        shippingRepo.save(shipping);
        profile.setShipping(shipping);
        profile.setUpdatedAt(new Time(System.currentTimeMillis()));
        profileRepo.save(profile);

        // update Stripe customer shipping data
        Stripe.apiKey = stripeApiKey;
        Map<String, Object> shipParams = new HashMap<>();
        Map<String, Object> addrParams = new HashMap<>();
        Map<String, Object> custParams = new HashMap<>();
        Customer customer = Customer.retrieve(profile.getStripeCustomerId());
        addrParams.put("city", body.getCity());
        addrParams.put("country", body.getCountry());
        addrParams.put("line1", body.getLine1());
        addrParams.put("line2", body.getLine2());
        addrParams.put("postal_code", body.getPostalCode());
        addrParams.put("state", body.getState());
        shipParams.put("address", addrParams);
        shipParams.put("name", profile.getFirstname() + " " + profile.getLastname());
        shipParams.put("phone", profile.getPhone());
        custParams.put("shipping", shipParams);
        customer.update(custParams);

        return getProfile(principal);
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
            Customer customer = customerService.createStripeCustomer(profile);
            profile.setStripeCustomerId(customer.getId());
            profile.setCreatedAt(new Time(System.currentTimeMillis()));
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

}

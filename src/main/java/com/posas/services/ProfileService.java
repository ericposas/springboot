package com.posas.services;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.posas.dtos.ProfileDTO;
import com.posas.entities.Address;
import com.posas.entities.Profile;
import com.posas.helpers.TokenHelpers;
import com.posas.repositories.AddressRepository;
import com.posas.repositories.ProfileRepository;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
class CreateUserProfileResponseDTO {
    UUID profileId;
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

    public JwtProfileDataDTO getJwtProfileData(Principal principal) {
        List<String> roles = TokenHelpers.getTokenResource(principal)
                .get(clientId)
                .get("roles");
        String scopesStr = (String) TokenHelpers.getTokenAttributes(principal).get("scope");
        List<String> scopes = List.of(scopesStr.split(" "));
        String name = TokenHelpers.getFromJwt(principal, "name");
        String preferredUsername = TokenHelpers.getFromJwt(principal, "preferred_username");
        String email = TokenHelpers.getFromJwt(principal, "email");

        System.out.print("\n\n");
        System.out.print("user: \n\n");
        System.out.print("preferred_username: \n");
        System.out.print(preferredUsername + "\n\n");
        System.out.print("email: \n");
        System.out.print(email);
        System.out.print("\n\n");

        String givenName = TokenHelpers.getFromJwt(principal, "given_name");
        String familyName = TokenHelpers.getFromJwt(principal, "family_name");
        String sid = TokenHelpers.getFromJwt(principal, "sid");
        String iss = TokenHelpers.getFromJwt(principal, "iss");
        Long authTime = (Long) TokenHelpers.getTokenAttributes(principal).get("auth_time");
        Boolean emailVerified = (Boolean) TokenHelpers.getTokenAttributes(principal).get("email_verified");

        Profile profileByEmail = profileRepo.findByEmail(email);
        Profile existingProfile = profileByEmail != null ? profileByEmail : createProfileFromJwtData(principal);

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

    public Profile createProfileFromJwtData(Principal principal) {
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
            profileRepo.saveAndFlush(profile);
            return profile;
        }
        if (email != null) return profileRepo.findByEmail(email);
        return null;
    }

    public CreateUserFromJwtAuthResponseDTO createUserProfileFromJwtAuthDataAndResponse(Principal principal) {
        Profile profile = createProfileFromJwtData(principal);
        if (profile != null) {
            return CreateUserFromJwtAuthResponseDTO.builder()
                    .profile(profile)
                    .message("Retrieved profile from authenticated user.")
                    .build();
        }
        return CreateUserFromJwtAuthResponseDTO.builder()
            .message("Missing email in user jwt.")
            .build();
    }

    public CreateUserProfileResponseDTO createUserProfile(ProfileDTO profileData)
            throws RuntimeException {

        Address address = new Address();
        if (profileData.getAddress() != null) {
            address.setStreetnum(profileData.getAddress().getStreetnum());
            address.setStreetname(profileData.getAddress().getStreetname());
            address.setCity(profileData.getAddress().getCity());
            address.setState(profileData.getAddress().getState());
            address.setPostalCode(profileData.getAddress().getPostalCode());
            addressRepo.save(address);
        }

        Profile profile = new Profile();
        profile.setFirstname(profileData.getFirstname());
        profile.setLastname(profileData.getLastname());
        profile.setEmail(profileData.getEmail());
        profileRepo.save(profile);

        if (profileData.getAddress() != null) {
            profile.setAddress(address);
            profileRepo.save(profile);
            address.setProfile(profile);
            addressRepo.save(address);
        }

        profileRepo.saveAndFlush(profile);

        return CreateUserProfileResponseDTO.builder()
                .message("Created new user profile")
                .profileId(profile.getProfileId())
                .build();
    }

}

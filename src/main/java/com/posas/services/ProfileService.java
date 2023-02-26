package com.posas.services;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
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
    UUID profile_id;
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
    String email;
    String preferredUsername;
    String firstname;
    String lastname;
    String message;
}

@Service
public class ProfileService {

    @Autowired
    ProfileRepository profileRepo;

    @Autowired
    AddressRepository addressRepo;

    public ListUserProfileResponseDTO getAllUserProfiles()
            throws RuntimeException {
        List<Profile> result = profileRepo.findAll();
        return ListUserProfileResponseDTO.builder()
                .profiles(result)
                .message("Retrieved list of user profiles")
                .build();
    }

    public CreateUserFromJwtAuthResponseDTO createUserProfileFromJwtAuthData(Principal principal)
            throws RuntimeException {
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

            return CreateUserFromJwtAuthResponseDTO.builder()
                    .email(email)
                    .preferredUsername(preferredUsername)
                    .firstname(firstname)
                    .lastname(lastname)
                    .message("Created new profile from authenticated user.")
                    .build();
        }

        return CreateUserFromJwtAuthResponseDTO.builder()
            .message("Either email is NULL or your profile already exists.")
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
                .profile_id(profile.getProfileId())
                .build();
    }

}

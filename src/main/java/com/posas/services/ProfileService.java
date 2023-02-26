package com.posas.services;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.posas.dtos.ProfileDTO;
import com.posas.entities.Address;
import com.posas.entities.Profile;
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

@Service
public class ProfileService {

    @Autowired
    ProfileRepository profileRepo;

    @Autowired
    AddressRepository addressRepo;

    public CreateUserProfileResponseDTO createUserProfile(ProfileDTO profileData) {

        Address address = Address.builder().build();
        if (profileData.getAddress() != null) {
            address = Address.builder()
                    .streetnum(profileData.getAddress().getStreetnum())
                    .streetname(profileData.getAddress().getStreetname())
                    .city(profileData.getAddress().getCity())
                    .state(profileData.getAddress().getState())
                    .postalCode(profileData.getAddress().getPostalCode())
                    .build();
            addressRepo.save(address);
        }

        Profile profile = Profile.builder()
                .firstname(profileData.getFirstname())
                .lastname(profileData.getLastname())
                .email(profileData.getEmail())
                .build();
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
                .profile_id(profile.getProfile_id())
                .build();
    }

}

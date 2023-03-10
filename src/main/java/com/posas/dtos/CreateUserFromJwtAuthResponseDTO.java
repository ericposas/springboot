package com.posas.dtos;

import com.posas.entities.Profile;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateUserFromJwtAuthResponseDTO {
    private Profile profile;
    private String message;
}
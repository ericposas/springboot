package com.posas.dtos;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttributesDTO {
    List<String> roles;
    List<String> scopes;
    String username;
    String message;
}
package com.microservice.user.dto;

import java.util.Set;

public record UserResponseDTO(String userId,String email, String username, String firstName, String lastName, Set<String> roles) {
}

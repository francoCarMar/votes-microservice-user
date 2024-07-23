package com.microservice.user.service;


import com.microservice.user.dto.UserDTO;
import com.microservice.user.dto.UserResponseDTO;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

public interface IKeycloakService {

    List<UserRepresentation> findAllUsers();
    List<UserRepresentation> searchUserByUsername(String username);
    UserResponseDTO searchUserByEmail(String email);
    String createUser(UserDTO userDTO);
    String createUsers(List<UserDTO> userDTOS);
    void deleteUser(String userId);
    void updateUser(String userId, UserDTO userDTO);
    void forgotPassword(String userId);
}


package com.microservice.user.service;


import com.microservice.user.dto.UserDTO;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

public interface IKeycloakService {

    List<UserRepresentation> findAllUsers();
    List<UserRepresentation> searchUserByUsername(String username);
    String createUser(UserDTO userDTO);
    String createUsers(List<UserDTO> userDTOS);
    void deleteUser(String userId);
    void updateUser(String userId, UserDTO userDTO);
}


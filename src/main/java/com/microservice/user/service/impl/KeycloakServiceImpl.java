package com.microservice.user.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.microservice.user.dto.UserDTO;
import com.microservice.user.dto.UserResponseDTO;
import com.microservice.user.service.IKeycloakService;
import com.microservice.user.service.SendMail;
import com.microservice.user.util.KeycloakProvider;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.util.Collections;

import static com.microservice.user.util.KeycloakProvider.getRealmResource;
import static com.microservice.user.util.KeycloakProvider.getUserResource;

@Service
@Slf4j
public class KeycloakServiceImpl implements IKeycloakService {

    @Autowired
    private SendMail sendMail;

    /**
     * Metodo para listar todos los usuarios de Keycloak
     *
     * @return List<UserRepresentation>
     */
    public List<UserRepresentation> findAllUsers() {
        return KeycloakProvider.getRealmResource()
                .users()
                .list();
    }


    /**
     * Metodo para buscar un usuario por su username
     *
     * @return List<UserRepresentation>
     */
    public List<UserRepresentation> searchUserByUsername(String username) {
        return KeycloakProvider.getRealmResource()
                .users()
                .searchByUsername(username, true);
    }

    @Override
    public UserResponseDTO searchUserByEmail(String email) {
        return KeycloakProvider.getUserByEmail(email);
    }

    /**
     * Metodo para crear un usuario en keycloak
     *
     * @return String
     */
    public String createUser(@NonNull UserDTO userDTO) {

        int status = 0;
        UsersResource usersResource = getUserResource();

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setFirstName(userDTO.getFirstName());
        userRepresentation.setLastName(userDTO.getLastName());
        userRepresentation.setEmail(userDTO.getEmail());
        userRepresentation.setUsername(userDTO.getUsername());
        userRepresentation.setEnabled(true);
        userRepresentation.setEmailVerified(true);

        Response response = usersResource.create(userRepresentation);

        status = response.getStatus();

        if (status == 201) {
            String path = response.getLocation().getPath();
            String userId = path.substring(path.lastIndexOf("/") + 1);

            CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
            credentialRepresentation.setTemporary(false);
            credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
            credentialRepresentation.setValue(userDTO.getPassword());

            usersResource.get(userId).resetPassword(credentialRepresentation);

            RealmResource realmResource = KeycloakProvider.getRealmResource();

            List<RoleRepresentation> rolesRepresentation = null;

            String statusMail = "";
            if (userDTO.getRoles() == null || userDTO.getRoles().isEmpty()) {
                rolesRepresentation = List.of(realmResource.roles().get("user").toRepresentation());
                String message = "Usuario: " + userDTO.getEmail() + "\nPassword: " + userDTO.getPassword();
                statusMail = "\nStatus Mail" + sendMail.sendMail(List.of(userDTO.getEmail()), "Credenciales EasyVote", message);
            } else {
                rolesRepresentation = realmResource.roles()
                        .list()
                        .stream()
                        .filter(role -> userDTO.getRoles()
                                .stream()
                                .anyMatch(roleName -> roleName.equalsIgnoreCase(role.getName())))
                        .toList();
            }

            realmResource.users().get(userId).roles().realmLevel().add(rolesRepresentation);

            return userDTO.getEmail() + ": User created successfully!!" + statusMail;

        } else if (status == 409) {
            log.error("User exist already!");
            return userDTO.getEmail() + " User exist already!";
        } else {
            log.error("Error creating user, please contact with the administrator.");
            return userDTO.getEmail() + " Error creating user, please contact with the administrator.";
        }
    }

    /**
     * Metodo para crear varios usuarios en keycloak
     *
     * @return String
     */
    public String createUsers(@NonNull List<UserDTO> usersDTO) {
        StringBuilder response = new StringBuilder();
        for (UserDTO userDTO : usersDTO) {
            response.append(createUser(userDTO)).append("\n");
        }
        return response.toString();
    }

    /**
     * Metodo para borrar un usuario en keycloak
     *
     * @return void
     */
    public void deleteUser(String userId) {
        getUserResource()
                .get(userId)
                .remove();
    }


    /**
     * Metodo para actualizar un usuario en keycloak
     *
     * @return void
     */
    public void updateUser(String userId, @NonNull UserDTO userDTO) {

        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType(OAuth2Constants.PASSWORD);
        credentialRepresentation.setValue(userDTO.getPassword());

        UserRepresentation user = new UserRepresentation();
        user.setUsername(userDTO.getUsername());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setCredentials(Collections.singletonList(credentialRepresentation));

        UserResource usersResource = getUserResource().get(userId);
        usersResource.update(user);
    }

    @Override
    public void forgotPassword(String email) {
        UsersResource usersResource = KeycloakProvider.getUserResource();
        List<UserRepresentation> representsClientList = usersResource.searchByEmail(email,true);
        UserRepresentation userRepresentation = representsClientList.stream().findFirst().orElse(null);
        if(userRepresentation != null){
            UserResource userResource = usersResource.get(userRepresentation.getId());
            List<String> actions = new ArrayList<>();
            actions.add("UPDATE_PASSWORD");
            userResource.executeActionsEmail(actions);
            return;
        }
        throw new RuntimeException("User not found");
    }

}
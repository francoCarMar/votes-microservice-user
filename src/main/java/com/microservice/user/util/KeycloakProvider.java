package com.microservice.user.util;


import com.microservice.user.dto.UserDTO;
import com.microservice.user.dto.UserResponseDTO;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class KeycloakProvider {

    private static final String SERVER_URL = "http://localhost:8081";
    private static final String REALM_NAME = "spring-boot-realm-dev";
    private static final String REALM_MASTER = "master";
    private static final String ADMIN_CLI = "admin-cli";
    private static final String USER_CONSOLE = "admin";
    private static final String PASSWORD_CONSOLE = "admin";
    private static final String CLIENT_SECRET = "admin";

    public static RealmResource getRealmResource() {
        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(SERVER_URL)
                .realm(REALM_MASTER)
                .clientId(ADMIN_CLI)
                .username(USER_CONSOLE)
                .password(PASSWORD_CONSOLE)
                .clientSecret(CLIENT_SECRET)
                .resteasyClient(new ResteasyClientBuilderImpl()
                        .connectionPoolSize(10)
                        .build())
                .build();

        return keycloak.realm(REALM_NAME);
    }

    public static UsersResource getUserResource() {
        RealmResource realmResource = getRealmResource();
        return realmResource.users();
    }

    public static UserResponseDTO getUserByEmail(String email){
        List<UserRepresentation> users = getUserResource().searchByEmail(email, true);
        if(users.isEmpty()){
            return null;
        }
        String userId = users.get(0).getId();
        List<RoleRepresentation> roles = getUserResource().get(userId).roles().realmLevel().listAll();
        Set<String> rolesNames = roles.stream().map(RoleRepresentation::getName).collect(Collectors.toSet());
        return new UserResponseDTO(userId,users.get(0).getEmail(), users.get(0).getUsername(), users.get(0).getFirstName(), users.get(0).getLastName(), rolesNames);
    }
}


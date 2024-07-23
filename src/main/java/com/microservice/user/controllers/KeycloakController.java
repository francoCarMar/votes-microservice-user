package com.microservice.user.controllers;


import com.microservice.user.dto.UserDTO;
import com.microservice.user.service.IKeycloakService;
import com.microservice.user.util.mappers.UserDTOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/keycloak/user")
public class KeycloakController {

    @Autowired
    private IKeycloakService keycloakService;

    @GetMapping("/search")
    public ResponseEntity<?> findAllUsers(){
        return ResponseEntity.ok(keycloakService.findAllUsers());
    }


    @GetMapping("/search/{username}")
    public ResponseEntity<?> searchUserByUsername(@PathVariable String username){
        return ResponseEntity.ok(keycloakService.searchUserByUsername(username));
    }

    @GetMapping("/search-email/{email}")
    public ResponseEntity<?> getUserResource(@PathVariable String email) {
        return ResponseEntity.ok(keycloakService.searchUserByEmail(email));
    }

    @PutMapping("/forgot-password/{email}")
    public ResponseEntity<?> updatePassword(@PathVariable String email){
        try {
            keycloakService.forgotPassword(email);
            return ResponseEntity.ok("Revisa tu correo para actualizar tu contraseña");
        }catch (Exception e){
            return ResponseEntity.badRequest().body("Error updating password : "+e.getMessage());
        }
    }


    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody UserDTO userDTO) throws URISyntaxException {
        String response = keycloakService.createUser(userDTO);
        return ResponseEntity.created(new URI("/keycloak/user/create")).body(response);
    }


    @PutMapping("/update/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable String userId, @RequestBody UserDTO userDTO){
        keycloakService.updateUser(userId, userDTO);
        return ResponseEntity.ok("User updated successfully");
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId){
        keycloakService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/create-users")
    public ResponseEntity<String> createUsers(@RequestParam("file") MultipartFile file){
        List<UserDTO> userDTOS = UserDTOMapper.CSVToUserDTO(file);
        String response = keycloakService.createUsers(userDTOS);
        return ResponseEntity.ok(response);
    }
}

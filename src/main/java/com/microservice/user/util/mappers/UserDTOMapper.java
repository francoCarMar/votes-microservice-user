package com.microservice.user.util.mappers;

import com.microservice.user.dto.UserDTO;
import com.microservice.user.util.PasswordGenerator;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class UserDTOMapper {

    public static List<UserDTO> CSVToUserDTO(MultipartFile file) {
        List<UserDTO> userDTOS = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                UserDTO user = UserDTO.builder()
                        .username(fields[0])
                        .email(fields[1])
                        .firstName(fields[2])
                        .lastName(fields[3])
                        .password(PasswordGenerator.generate())
                        .build();
                userDTOS.add(user);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return userDTOS;
    }
}

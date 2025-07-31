package com.Huy.user_service.util;

import org.springframework.stereotype.Component;

import com.Huy.user_service.data.activation;
import com.Huy.user_service.data.roles;
import com.Huy.user_service.dto.request.CreateUserDTO;
import com.Huy.user_service.model.Users;

@Component
public class Utils {
    public static Users mapCreateUserDTOtoUsers(CreateUserDTO createUserDTO) {
        return Users.builder()
                .id(java.util.UUID.randomUUID().toString()) // Generate a random UUID for the user ID
                .username(createUserDTO.getUsername())
                .password(createUserDTO.getPassword())
                .email(createUserDTO.getEmail())
                .phone(createUserDTO.getPhone())
                .address(createUserDTO.getAddress())
                .role(roles.USER.toString()) // Default role set to USER
                .active(activation.INACTIVATE.toString())
                .verificationCode(java.util.UUID.randomUUID().toString())
                .build();
    }
}

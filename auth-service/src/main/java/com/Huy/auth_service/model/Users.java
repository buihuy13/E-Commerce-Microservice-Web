package com.Huy.auth_service.model;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Component
public class Users {
    private String id;
    private String username;
    private String password;
    private String email;
    private String phone;
    private String address;
    private String role;
    private String verificationCode;
    private String active;
}

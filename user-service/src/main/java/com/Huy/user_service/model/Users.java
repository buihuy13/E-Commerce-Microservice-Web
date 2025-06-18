package com.Huy.user_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Users {
    @Id
    private String id;
    @NotBlank(message = "Username is mandatory")
    private String username;
    @NotBlank(message = "Password is mandatory")
    private String password;
    @Email
    @NotBlank(message = "Email is mandatory")
    private String email;
    @NotBlank(message = "Phone number is mandatory")
    private String phone;
    @NotBlank(message = "Address is mandatory")
    private String address;
    @NotBlank(message = "Role is mandatory")
    private String role;
}

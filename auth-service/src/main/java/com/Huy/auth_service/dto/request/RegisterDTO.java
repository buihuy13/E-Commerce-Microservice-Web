package com.Huy.auth_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDTO {
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
}

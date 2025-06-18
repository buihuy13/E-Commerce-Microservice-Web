package com.Huy.user_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserDTO {
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

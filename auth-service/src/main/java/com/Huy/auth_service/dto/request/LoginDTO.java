package com.Huy.auth_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginDTO {
    @NotBlank(message = "Email is mandatory")
    @Email
    private String email;
    @NotBlank(message = "Password is mandatory")
    private String password;
}

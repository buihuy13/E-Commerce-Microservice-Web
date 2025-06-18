package com.Huy.auth_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UserDetails {
    private String id;
    private String username;
    private String email;
    private String phone;
    private String address;
    private String role;
}

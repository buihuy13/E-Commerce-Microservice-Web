package com.Huy.auth_service.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.Huy.Common.Exception.InvalidParameters;
import com.Huy.Common.Exception.ResourceNotFoundException;
import com.Huy.auth_service.dto.request.LoginDTO;
import com.Huy.auth_service.dto.request.RegisterDTO;
import com.Huy.auth_service.dto.response.TokenResponse;
import com.Huy.auth_service.dto.response.UserDetails;
import com.Huy.auth_service.model.Users;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthService {
    private final BCryptPasswordEncoder passwordEncoder;
    private final WebClient.Builder webClientBuilder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(BCryptPasswordEncoder passwordEncoder, WebClient.Builder webClientBuilder,
                       AuthenticationManager authenticationManager, JwtService jwtService) {
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.webClientBuilder = webClientBuilder;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerUser(RegisterDTO registerDTO) throws InvalidParameters {
        registerDTO.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        try {
            webClientBuilder.build()
                .post()
                .uri("lb://user-service/api/users")
                .bodyValue(registerDTO)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
        }
        catch(WebClientResponseException e)
        {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {

                log.error("Invalid param", e);
                throw new InvalidParameters("Dữ liệu đầu vào không hợp lệ");
            }
            log.error("Error during registration: {}", e.getMessage(), e); 
            throw new RuntimeException("Error during registration: " + e.getMessage(), e);
        }
    }

    public TokenResponse login(LoginDTO model) {
        @SuppressWarnings("unused")
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(model.getEmail(), model.getPassword()));
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"))
                .getAuthority();

        System.out.println("Role: " + role);
        return new TokenResponse(jwtService.generateToken(model.getEmail(), role), jwtService.generateRefreshToken(model.getEmail(), role));
    }

    public String refreshAccessToken(String refToken) throws Exception {
        return jwtService.refreshAccessToken(refToken);
    }

    public UserDetails getUserByAccessToken(String accessToken) throws Exception {
        String email = jwtService.extractUserName(accessToken);
        Users user = webClientBuilder.build()
                .get()
                .uri("lb://user-service/api/users/email/{email}", email)
                .retrieve()
                .bodyToMono(Users.class)
                .block();

        if (user == null)
        {
            throw new ResourceNotFoundException("Cannot find user with email extracted in access token");
        }

        return UserDetails.builder()
                   .address(user.getAddress())
                   .email(email)
                   .phone(user.getPhone())
                   .username(user.getUsername())
                   .id(user.getId())
                   .role(user.getRole()).build();  
    }
}

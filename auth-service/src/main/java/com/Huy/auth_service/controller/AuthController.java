package com.Huy.auth_service.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Huy.Common.Exception.InvalidParameters;
import com.Huy.auth_service.dto.request.LoginDTO;
import com.Huy.auth_service.dto.request.RegisterDTO;
import com.Huy.auth_service.dto.response.TokenResponse;
import com.Huy.auth_service.dto.response.UserDetails;
import com.Huy.auth_service.service.AuthService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService)
    {
        this.authService = authService;
    }

    public record MessageResponse(String message) {
    }

    @PostMapping("/register")
    @CircuitBreaker(name = "register_user", fallbackMethod = "fallbackMethod")
    @TimeLimiter(name = "register_user")
    @Retry(name = "register_user")
    public CompletableFuture<ResponseEntity<MessageResponse>> register(@RequestBody @Valid RegisterDTO registerDTO) throws InvalidParameters
    {
        authService.registerUser(registerDTO);
        return CompletableFuture.supplyAsync(() -> ResponseEntity.status(HttpStatus.CREATED).body((new MessageResponse("Tạo tài khoản thành công"))));
    }

    public CompletableFuture<String> fallbackMethod(RegisterDTO registerDTO, RuntimeException ex)
    {
        return CompletableFuture.supplyAsync(() -> "Oops! Something went wrong, please wait for 5 more minutes~");
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginDTO model) {
        TokenResponse tokens = authService.login(model);
        return ResponseEntity.ok(tokens);
    }

    @GetMapping("/accesstoken")
    public ResponseEntity<?> RefreshAccessToken(@RequestHeader("Refresh-Token") String refToken) throws Exception 
    {
        if (refToken.startsWith("Bearer"))
        {
            refToken = refToken.substring(7);
        }
        String accessToken = authService.refreshAccessToken(refToken);
        Map<String,String> response = new HashMap<>();
        response.put("accessToken", accessToken);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user")
    @CircuitBreaker(name = "register_user", fallbackMethod = "fallbackMethod")
    @TimeLimiter(name = "register_user")
    @Retry(name = "register_user")
    public ResponseEntity<UserDetails> getUserByAccessToken(@RequestHeader("Authorization") String accessToken) throws Exception {
        accessToken = accessToken.substring(7);
        UserDetails user = authService.getUserByAccessToken(accessToken);
        return ResponseEntity.ok(user);
    }
}

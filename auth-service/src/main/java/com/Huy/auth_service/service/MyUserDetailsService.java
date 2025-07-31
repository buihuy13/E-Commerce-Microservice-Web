package com.Huy.auth_service.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.Huy.auth_service.model.UserPrinciple;
import com.Huy.auth_service.model.Users;

@Service
public class MyUserDetailsService implements UserDetailsService {

    private final WebClient.Builder webClientBuilder;

    public MyUserDetailsService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = webClientBuilder.build()
                .get()
                .uri("lb://user-service/api/users/email/{email}", username)
                .retrieve()
                .bodyToMono(Users.class)
                .block();   
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        } 
        return new UserPrinciple(user);
    }

}

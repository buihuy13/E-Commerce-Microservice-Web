package com.Huy.user_service.service;

import java.util.List;

import org.springframework.stereotype.Service;
import com.Huy.user_service.dto.request.CreateUserDTO;
import com.Huy.user_service.dto.request.UpdateUserDTO;
import com.Huy.user_service.model.Users;
import com.Huy.user_service.repository.UserRepository;
import com.Huy.user_service.util.Utils;

import jakarta.transaction.Transactional;

import com.Huy.Common.Exception.ResourceNotFoundException;

@Service
public class UserService {
    private final UserRepository userRepository;
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }   

    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }

    public Users getUserById(String id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public Users getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public void createUsers(CreateUserDTO users) {
        Users newUser = Utils.mapCreateUserDTOtoUsers(users);
        userRepository.save(newUser);
    }

    @Transactional
    public Users updateUser(String id, UpdateUserDTO users) {
        Users existingUser = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        existingUser.setUsername(users.getUsername());
        existingUser.setEmail(users.getEmail());
        existingUser.setPhone(users.getPhone());
        existingUser.setAddress(users.getAddress());
        return userRepository.save(existingUser);
    }

    public void deleteUser(String id) {
        Users existingUser = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userRepository.delete(existingUser);
    }
}

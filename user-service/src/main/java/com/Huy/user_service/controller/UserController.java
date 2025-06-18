package com.Huy.user_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Huy.user_service.dto.request.CreateUserDTO;
import com.Huy.user_service.dto.request.UpdateUserDTO;
import com.Huy.user_service.model.Users;
import com.Huy.user_service.service.UserService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    record MessageResponse(String message) {
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Users> getUserByEmail(@PathVariable String email) {
        Users user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @GetMapping("")
    public ResponseEntity<List<Users>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Users> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping("")
    public ResponseEntity<Void> createUser(@RequestBody @Valid CreateUserDTO createUserDTO) {
        userService.createUsers(createUserDTO);
        return ResponseEntity.status(201).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Users> updateUser(@PathVariable String id, @RequestBody @Valid UpdateUserDTO updateUserDTO) {
        Users updatedUser = userService.updateUser(id, updateUserDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(new MessageResponse("User deleted successfully"));
    }
}

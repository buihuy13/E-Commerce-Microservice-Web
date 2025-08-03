package com.Huy.user_service.service;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.Huy.user_service.data.activation;
import com.Huy.user_service.dto.request.CreateUserDTO;
import com.Huy.user_service.dto.request.UpdateUserDTO;
import com.Huy.user_service.model.Users;
import com.Huy.user_service.repository.UserRepository;
import com.Huy.user_service.util.Utils;

import jakarta.transaction.Transactional;

import com.Huy.Common.Event.ConfirmationEvent;
import com.Huy.Common.Exception.ResourceNotFoundException;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final KafkaTemplate kafkaTemplate;
    public UserService(UserRepository userRepository, KafkaTemplate kafkaTemplate) {
        this.userRepository = userRepository;
        this.kafkaTemplate = kafkaTemplate;
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
    public String createUsers(CreateUserDTO users) {
        Users newUser = Utils.mapCreateUserDTOtoUsers(users);
        userRepository.save(newUser);
        return newUser.getVerificationCode();
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

    public void acitvateAccount(String code) throws SQLIntegrityConstraintViolationException {
        Users user = userRepository.findByVerificationCode(code);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with verification code: " + code);
        }
        if (user.getActive().equals(activation.ACTIVATE.toString())) {
            throw new SQLIntegrityConstraintViolationException("Account is already activated");
        }
        user.setActive(activation.ACTIVATE.toString());
        userRepository.save(user);
    }

    public void sendVerificationEmail(String email) {
        Users user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with email: " + email);
        }
        String verificationCode = user.getVerificationCode();
        kafkaTemplate.send("confirmationTopic", new ConfirmationEvent(email, "api/users/confirm?code=" + verificationCode));
    }
}

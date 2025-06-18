package com.Huy.user_service.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Huy.user_service.model.Users;

@Repository
public interface UserRepository extends JpaRepository<Users, String> {

    Users findByEmail(String email);
}

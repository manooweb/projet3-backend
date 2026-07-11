package com.chatop.api.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chatop.api.user.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {

    boolean existsByEmailIgnoreCase(String email);
}

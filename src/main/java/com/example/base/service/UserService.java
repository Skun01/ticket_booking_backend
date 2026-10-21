package com.example.base.service;

import java.util.Optional;

import com.example.base.domain.User;
import com.example.base.domain.enums.Role;

public interface UserService {

	User getById(Long id);

	Optional<User> findByEmail(String email);

	User create(String email, String rawPassword, String fullName, Role role);

}

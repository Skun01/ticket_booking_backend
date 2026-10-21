package com.example.base.dto.auth;

import com.example.base.domain.User;
import com.example.base.domain.enums.Role;

public record UserResponse(Long id, String email, String fullName, Role role) {

	public static UserResponse from(User user) {
		return new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole());
	}

}

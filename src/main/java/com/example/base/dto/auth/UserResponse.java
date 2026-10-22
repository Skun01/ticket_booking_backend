package com.example.base.dto.auth;

import com.example.base.domain.User;
import com.example.base.domain.enums.Role;
import com.example.base.domain.enums.UserStatus;

public record UserResponse(Long id, String email, String fullName, String phone, Role role, UserStatus status) {

	public static UserResponse from(User user) {
		return new UserResponse(user.getId(), user.getEmail(), user.getFullName(),
				user.getPhone(), user.getRole(), user.getStatus());
	}

}

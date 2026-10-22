package com.example.base.controller;

import com.example.base.annotation.CurrentUser;
import com.example.base.dto.auth.UserResponse;
import com.example.base.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@Operation(summary = "Lấy thông tin người dùng hiện tại")
	@GetMapping("/me")
	public UserResponse me(@CurrentUser Jwt jwt) {
		return UserResponse.from(userService.getById(Long.valueOf(jwt.getSubject())));
	}

}

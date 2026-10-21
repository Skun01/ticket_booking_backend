package com.example.base.service;

import com.example.base.dto.auth.AuthTokens;
import com.example.base.dto.auth.LoginRequest;
import com.example.base.dto.auth.RegisterRequest;

public interface AuthService {

	AuthTokens register(RegisterRequest request);

	AuthTokens login(LoginRequest request);

	AuthTokens refresh(String rawRefreshToken);

	void logout(String rawRefreshToken);

}

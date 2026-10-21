package com.example.base.service;

import com.example.base.domain.User;

public interface TokenService {

	String createAccessToken(User user);

	String generateRefreshToken();

	String hash(String token);

}

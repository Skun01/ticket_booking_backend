package com.example.base.dto.auth;

public record TokenResponse(String accessToken, String tokenType, long expiresIn) {
}

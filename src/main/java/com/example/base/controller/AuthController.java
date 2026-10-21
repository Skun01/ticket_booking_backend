package com.example.base.controller;

import java.time.Duration;

import com.example.base.config.property.JwtProperties;
import com.example.base.dto.auth.AuthTokens;
import com.example.base.dto.auth.LoginRequest;
import com.example.base.dto.auth.RegisterRequest;
import com.example.base.dto.auth.TokenResponse;
import com.example.base.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private static final String REFRESH_COOKIE = "refresh_token";

	private final AuthService authService;
	private final JwtProperties jwtProperties;

	@PostMapping("/register")
	public ResponseEntity<TokenResponse> register(@Valid @RequestBody RegisterRequest request) {
		return tokenResponse(HttpStatus.CREATED, authService.register(request));
	}

	@PostMapping("/login")
	public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
		return tokenResponse(HttpStatus.OK, authService.login(request));
	}

	@PostMapping("/refresh")
	public ResponseEntity<TokenResponse> refresh(
			@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
		return tokenResponse(HttpStatus.OK, authService.refresh(refreshToken));
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(
			@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
		if (refreshToken != null) {
			authService.logout(refreshToken);
		}
		return ResponseEntity.noContent()
				.header(HttpHeaders.SET_COOKIE, refreshCookie("", Duration.ZERO).toString())
				.build();
	}

	private ResponseEntity<TokenResponse> tokenResponse(HttpStatus status, AuthTokens tokens) {
		ResponseCookie cookie = refreshCookie(tokens.refreshToken(), jwtProperties.refreshTokenTtl());
		TokenResponse body = new TokenResponse(tokens.accessToken(), "Bearer",
				jwtProperties.accessTokenTtl().toSeconds());

		return ResponseEntity.status(status)
				.header(HttpHeaders.SET_COOKIE, cookie.toString())
				.body(body);
	}

	// đính kèm refresh token vào HTTP cookie
	private ResponseCookie refreshCookie(String value, Duration maxAge) {
		return ResponseCookie.from(REFRESH_COOKIE, value)
				.httpOnly(true)
				.secure(jwtProperties.refreshCookieSecure())
				.sameSite("Strict")
				.path("/api/auth")
				.maxAge(maxAge)
				.build();
	}

}

package com.example.base.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

	INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid email or password"),
	INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid refresh token");

	private final HttpStatus status;
	private final String message;

}

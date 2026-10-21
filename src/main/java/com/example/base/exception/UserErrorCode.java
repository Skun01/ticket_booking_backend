package com.example.base.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
	EMAIL_ALREADY_REGISTERED(HttpStatus.CONFLICT, "Email already registered");

	private final HttpStatus status;
	private final String message;

}

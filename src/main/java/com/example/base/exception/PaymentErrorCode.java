package com.example.base.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {

	PAYMENT_LINK_CREATION_FAILED(HttpStatus.BAD_GATEWAY, "Failed to create payment link with PayOS"),
	PAYMENT_LINK_NOT_FOUND(HttpStatus.NOT_FOUND, "Payment link not found"),
	INVALID_PAYMENT_WEBHOOK(HttpStatus.BAD_REQUEST, "Invalid payment webhook signature or data");

	private final HttpStatus status;
	private final String message;

}

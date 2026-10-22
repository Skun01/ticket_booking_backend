package com.example.base.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements ErrorCode {

	INVALID_ORDER_MESSAGE(HttpStatus.BAD_REQUEST, "Order message is invalid or does not match persisted order"),
	MAIL_DELIVERY_NOT_PREPARED(HttpStatus.INTERNAL_SERVER_ERROR, "Mail delivery receipt was not prepared");

	private final HttpStatus status;
	private final String message;

}

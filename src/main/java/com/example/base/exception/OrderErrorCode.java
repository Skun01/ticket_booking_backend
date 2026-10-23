package com.example.base.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {

	ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "Order not found"),
	ORDER_NOT_OWNED(HttpStatus.FORBIDDEN, "Order belongs to another customer"),
	CUSTOMER_INACTIVE(HttpStatus.FORBIDDEN, "Customer account is not active"),
	INVALID_ORDER_QUANTITY(HttpStatus.BAD_REQUEST, "Quantity must be between 1 and 100 and within max per order"),
	EVENT_NOT_BOOKABLE(HttpStatus.CONFLICT, "Event is not open for booking or payment"),
	SALE_NOT_OPEN(HttpStatus.CONFLICT, "Event ticket sales have not opened"),
	SOLD_OUT(HttpStatus.CONFLICT, "Not enough tickets available"),
	INVENTORY_CONFLICT(HttpStatus.CONFLICT, "Ticket inventory could not be released"),
	ORDER_NOT_PENDING(HttpStatus.CONFLICT, "Order is no longer pending"),
	ORDER_EXPIRED(HttpStatus.CONFLICT, "Order has expired"),
	PAYMENT_DECLINED(HttpStatus.PAYMENT_REQUIRED, "Simulated payment was declined");

	private final HttpStatus status;
	private final String message;

}

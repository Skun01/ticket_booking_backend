package com.example.base.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TicketTypeErrorCode implements ErrorCode {

	TICKET_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "Ticket type not found"),
	INVALID_SALE_TIME(HttpStatus.BAD_REQUEST, "Sale interval must be ordered and end before the event starts"),
	INVALID_TICKET_QUANTITY(HttpStatus.BAD_REQUEST, "Max per order cannot exceed total quantity"),
	QUANTITY_BELOW_SOLD(HttpStatus.CONFLICT, "Total quantity cannot be lower than reserved or sold quantity");

	private final HttpStatus status;
	private final String message;

}

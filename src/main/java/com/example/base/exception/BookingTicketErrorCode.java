package com.example.base.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BookingTicketErrorCode implements ErrorCode {

	TICKET_NOT_FOUND(HttpStatus.NOT_FOUND, "Ticket not found"),
	TICKET_NOT_OWNED_BY_ORGANIZER(HttpStatus.FORBIDDEN, "Ticket belongs to another organizer's event"),
	TICKET_ALREADY_USED(HttpStatus.CONFLICT, "Ticket has already been checked in"),
	TICKET_NOT_VALID(HttpStatus.CONFLICT, "Ticket is not valid for check-in"),
	EVENT_NOT_OPEN_FOR_CHECKIN(HttpStatus.CONFLICT, "Event is cancelled, ended or no longer published");

	private final HttpStatus status;
	private final String message;

}

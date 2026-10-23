package com.example.base.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum EventErrorCode implements ErrorCode {

	EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Event not found"),
	EVENT_NOT_OWNED(HttpStatus.FORBIDDEN, "You do not own this event"),
	INVALID_EVENT_TIME(HttpStatus.BAD_REQUEST, "End time must be after start time"),
	INVALID_SALES_START_TIME(HttpStatus.BAD_REQUEST, "Sales start time must be before event start time"),
	INVALID_EVENT_FILTER_TIME(HttpStatus.BAD_REQUEST, "Filter from must be before to"),
	INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "Invalid event status transition"),
	EVENT_NOT_EDITABLE(HttpStatus.CONFLICT, "Only draft events can be configured"),
	EVENT_START_IN_PAST(HttpStatus.BAD_REQUEST, "Event must start in the future"),
	EVENT_HAS_NO_TICKET_TYPES(HttpStatus.CONFLICT, "Add at least one ticket type before publishing"),
	VENUE_CAPACITY_EXCEEDED(HttpStatus.CONFLICT, "Ticket quantity exceeds venue capacity");

	private final HttpStatus status;
	private final String message;

}

package com.example.base.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CatalogErrorCode implements ErrorCode {

	CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "Category not found"),
	CATEGORY_SLUG_EXISTS(HttpStatus.CONFLICT, "Category slug already exists"),
	VENUE_NOT_FOUND(HttpStatus.NOT_FOUND, "Venue not found"),
	CATEGORY_IN_USE(HttpStatus.CONFLICT, "Category is used by an event"),
	VENUE_IN_USE(HttpStatus.CONFLICT, "Venue is used by an event"),
	VENUE_CAPACITY_IN_USE(HttpStatus.CONFLICT, "Cannot change capacity of a venue used by events");

	private final HttpStatus status;
	private final String message;

}

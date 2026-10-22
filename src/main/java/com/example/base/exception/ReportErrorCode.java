package com.example.base.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReportErrorCode implements ErrorCode {

	INVALID_REPORT_RANGE(HttpStatus.BAD_REQUEST, "Report from and to are required, and from must be before to");

	private final HttpStatus status;
	private final String message;

}

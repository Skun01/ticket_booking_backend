package com.example.base.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PdfErrorCode implements ErrorCode {

	PDF_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Could not generate PDF"),
	QR_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Could not generate ticket QR code");

	private final HttpStatus status;
	private final String message;

}

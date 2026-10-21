package com.example.base.exception;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

// trả về problem detail format cho mọi lỗi
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	private static final String CODE = "code";

	@ExceptionHandler(AppException.class)
	ResponseEntity<ProblemDetail> handleAppException(AppException ex) {
		ErrorCode errorCode = ex.getErrorCode();
		return problem(errorCode.getStatus(), errorCode.name(), ex.getMessage());
	}

	// xử lý 2 lỗi trả về từ security filter
	@ExceptionHandler(AuthenticationException.class)
	ResponseEntity<ProblemDetail> handleAuthentication(AuthenticationException ex) {
		return problem(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication required");
	}

	@ExceptionHandler(AccessDeniedException.class)
	ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex) {
		return problem(HttpStatus.FORBIDDEN, "FORBIDDEN", "Access denied");
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ProblemDetail> handleUnexpected(Exception ex) {
		log.error("Unhandled exception", ex);

		return problem(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unexpected error");
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		List<FieldViolation> errors = ex.getBindingResult().getAllErrors().stream()
				.map(error -> new FieldViolation(
						error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName(),
						error.getDefaultMessage()))
				.toList();
		ProblemDetail body = ex.getBody();
		body.setDetail("Validation failed");
		body.setProperty(CODE, "VALIDATION_FAILED");
		body.setProperty("errors", errors);

		return handleExceptionInternal(ex, body, headers, status, request);
	}

	// Lỗi Spring MVC tự xử lý: sai method, body sai JSON, thiếu param; lấy HTTP status làm mã lỗi
	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
			HttpStatusCode statusCode, WebRequest request) {
		ResponseEntity<Object> response = super.handleExceptionInternal(ex, body, headers, statusCode, request);
		if (response != null && response.getBody() instanceof ProblemDetail problem
				&& (problem.getProperties() == null || !problem.getProperties().containsKey(CODE))) {
			HttpStatus status = HttpStatus.resolve(statusCode.value());
			problem.setProperty(CODE, status != null ? status.name() : "ERROR");
		}
		return response;
	}

	private static ResponseEntity<ProblemDetail> problem(HttpStatus status, String code, String detail) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setProperty(CODE, code);
		return ResponseEntity.status(status).body(problem);
	}

	public record FieldViolation(String field, String message) {
	}

}

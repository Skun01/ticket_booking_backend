package com.example.base.exception;

public class InvalidOrderMessageException extends AppException {

	public InvalidOrderMessageException() {
		super(NotificationErrorCode.INVALID_ORDER_MESSAGE);
	}

}

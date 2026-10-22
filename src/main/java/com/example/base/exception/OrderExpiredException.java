package com.example.base.exception;

public class OrderExpiredException extends AppException {

	public OrderExpiredException() {
		super(OrderErrorCode.ORDER_EXPIRED);
	}

}

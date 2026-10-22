package com.example.base.exception;

import java.math.BigDecimal;

import com.example.base.domain.enums.PaymentMethod;
import lombok.Getter;

@Getter
public class PaymentDeclinedException extends AppException {

	private final Long orderId;
	private final PaymentMethod method;
	private final BigDecimal amount;

	public PaymentDeclinedException(Long orderId, PaymentMethod method, BigDecimal amount) {
		super(OrderErrorCode.PAYMENT_DECLINED);
		this.orderId = orderId;
		this.method = method;
		this.amount = amount;
	}

}

package com.example.base.service;

import java.math.BigDecimal;

import com.example.base.domain.enums.PaymentMethod;

public interface PaymentAuditService {

	void recordFailure(Long orderId, PaymentMethod method, BigDecimal amount);

}

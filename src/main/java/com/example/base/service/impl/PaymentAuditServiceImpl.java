package com.example.base.service.impl;

import java.math.BigDecimal;

import com.example.base.domain.Payment;
import com.example.base.domain.enums.PaymentMethod;
import com.example.base.repository.OrderRepository;
import com.example.base.repository.PaymentRepository;
import com.example.base.service.PaymentAuditService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentAuditServiceImpl implements PaymentAuditService {

	private final PaymentRepository paymentRepository;
	private final OrderRepository orderRepository;

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void recordFailure(Long orderId, PaymentMethod method, BigDecimal amount) {
		Payment payment = Payment.failed(orderRepository.getReferenceById(orderId), method, amount);
		paymentRepository.save(payment);
	}

}

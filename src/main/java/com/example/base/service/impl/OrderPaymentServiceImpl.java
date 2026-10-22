package com.example.base.service.impl;

import com.example.base.dto.order.OrderResponse;
import com.example.base.dto.payment.PayOrderRequest;
import com.example.base.exception.PaymentDeclinedException;
import com.example.base.service.OrderPaymentService;
import com.example.base.service.OrderService;
import com.example.base.service.PaymentAuditService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderPaymentServiceImpl implements OrderPaymentService {

	private final OrderService orderService;
	private final PaymentAuditService paymentAuditService;

	@Override
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public OrderResponse pay(Long customerId, Long orderId, PayOrderRequest request) {
		try {
			return orderService.pay(customerId, orderId, request);
		}
		catch (PaymentDeclinedException ex) {
			// Chờ transaction giữ khóa order rollback xong để FK của bản ghi FAILED không tự chờ khóa cha.
			paymentAuditService.recordFailure(ex.getOrderId(), ex.getMethod(), ex.getAmount());
			throw ex;
		}
	}

}

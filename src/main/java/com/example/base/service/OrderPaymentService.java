package com.example.base.service;

import com.example.base.dto.order.OrderResponse;
import com.example.base.dto.payment.PayOrderRequest;

public interface OrderPaymentService {

	OrderResponse pay(Long customerId, Long orderId, PayOrderRequest request);

}

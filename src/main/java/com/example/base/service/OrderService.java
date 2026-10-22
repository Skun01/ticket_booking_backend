package com.example.base.service;

import com.example.base.dto.common.PageResponse;
import com.example.base.dto.order.CreateOrderRequest;
import com.example.base.dto.order.OrderResponse;
import com.example.base.dto.order.OrderSummaryResponse;
import com.example.base.dto.payment.PayOrderRequest;

import org.springframework.data.domain.Pageable;

public interface OrderService {

	OrderResponse create(Long customerId, CreateOrderRequest request);

	OrderResponse pay(Long customerId, Long orderId, PayOrderRequest request);

	OrderResponse cancel(Long customerId, Long orderId);

	PageResponse<OrderSummaryResponse> listOwn(Long customerId, Pageable pageable);

	OrderResponse getOwn(Long customerId, Long orderId);

}

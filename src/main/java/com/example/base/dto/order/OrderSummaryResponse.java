package com.example.base.dto.order;

import java.math.BigDecimal;
import java.time.Instant;

import com.example.base.domain.Order;
import com.example.base.domain.enums.OrderStatus;

public record OrderSummaryResponse(Long id, String orderCode, OrderStatus status, BigDecimal totalAmount,
		Instant expiresAt, Instant createdAt) {

	public static OrderSummaryResponse from(Order order) {
		return new OrderSummaryResponse(order.getId(), order.getOrderCode(), order.getStatus(),
				order.getTotalAmount(), order.getExpiresAt(), order.getCreatedAt());
	}

}

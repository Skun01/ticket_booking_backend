package com.example.base.dto.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.example.base.domain.Order;
import com.example.base.domain.enums.OrderStatus;

public record OrderMessage(String messageId, Long orderId, String orderCode, Long userId, String email,
		OrderStatus status, BigDecimal totalAmount, Instant occurredAt) {

	public static OrderMessage from(Order order, Instant occurredAt) {
		return new OrderMessage(UUID.randomUUID().toString(), order.getId(), order.getOrderCode(),
				order.getUser().getId(), order.getUser().getEmail(), order.getStatus(),
				order.getTotalAmount(), occurredAt);
	}

}

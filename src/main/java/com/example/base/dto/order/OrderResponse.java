package com.example.base.dto.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.example.base.domain.Order;
import com.example.base.domain.enums.OrderStatus;

public record OrderResponse(Long id, String orderCode, OrderStatus status, BigDecimal totalAmount,
		BigDecimal discountAmount, Instant expiresAt, Instant createdAt, List<OrderItemResponse> items) {

	public static OrderResponse from(Order order) {
		List<OrderItemResponse> items = order.getItems().stream().map(OrderItemResponse::from).toList();

		return new OrderResponse(order.getId(), order.getOrderCode(), order.getStatus(), order.getTotalAmount(),
				order.getDiscountAmount(), order.getExpiresAt(), order.getCreatedAt(), items);
	}

}

package com.example.base.dto.order;

import java.math.BigDecimal;

import com.example.base.domain.OrderItem;
import com.example.base.domain.TicketType;

public record OrderItemResponse(Long id, Long ticketTypeId, String ticketTypeName, Long eventId,
		int quantity, BigDecimal unitPrice) {

	public static OrderItemResponse from(OrderItem item) {
		TicketType ticketType = item.getTicketType();

		return new OrderItemResponse(item.getId(), ticketType.getId(), ticketType.getName(),
				ticketType.getEvent().getId(), item.getQuantity(), item.getUnitPrice());
	}

}

package com.example.base.dto.ticket;

import java.math.BigDecimal;

import com.example.base.domain.TicketType;

public record TicketTypeResponse(Long id, Long eventId, String name, BigDecimal price, int totalQuantity,
		int soldQuantity, int availableQuantity, int maxPerOrder) {

	public static TicketTypeResponse from(TicketType ticketType) {
		return new TicketTypeResponse(ticketType.getId(), ticketType.getEvent().getId(), ticketType.getName(),
				ticketType.getPrice(), ticketType.getTotalQuantity(), ticketType.getSoldQuantity(),
				ticketType.getAvailableQuantity(), ticketType.getMaxPerOrder());
	}

}

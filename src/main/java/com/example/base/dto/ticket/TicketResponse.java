package com.example.base.dto.ticket;

import java.time.Instant;

import com.example.base.domain.Event;
import com.example.base.domain.Ticket;
import com.example.base.domain.TicketType;
import com.example.base.domain.enums.TicketStatus;

public record TicketResponse(Long id, String ticketCode, TicketStatus status, Instant checkedInAt, Long orderId,
		Long eventId, String eventTitle, Instant startTime, Instant endTime, Long ticketTypeId, String ticketTypeName) {

	public static TicketResponse from(Ticket ticket) {
		TicketType ticketType = ticket.getOrderItem().getTicketType();
		Event event = ticketType.getEvent();

		return new TicketResponse(ticket.getId(), ticket.getTicketCode(), ticket.getStatus(), ticket.getCheckedInAt(),
				ticket.getOrderItem().getOrder().getId(), event.getId(), event.getTitle(), event.getStartTime(),
				event.getEndTime(), ticketType.getId(), ticketType.getName());
	}

}

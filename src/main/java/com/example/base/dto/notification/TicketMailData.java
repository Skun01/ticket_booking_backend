package com.example.base.dto.notification;

import java.time.Instant;

import com.example.base.domain.Event;
import com.example.base.domain.Ticket;
import com.example.base.domain.TicketType;
import com.example.base.domain.Venue;
import com.example.base.domain.enums.TicketStatus;

public record TicketMailData(String ticketCode, String ticketTypeName, TicketStatus status,
		String eventTitle, Instant startTime, Instant endTime, String venueName, String address, String city) {

	public static TicketMailData from(Ticket ticket) {
		TicketType ticketType = ticket.getOrderItem().getTicketType();
		Event event = ticketType.getEvent();
		Venue venue = event.getVenue();

		return new TicketMailData(ticket.getTicketCode(), ticketType.getName(), ticket.getStatus(), event.getTitle(),
				event.getStartTime(), event.getEndTime(), venue.getName(), venue.getAddress(), venue.getCity());
	}

}

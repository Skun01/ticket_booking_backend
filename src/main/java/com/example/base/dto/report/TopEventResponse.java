package com.example.base.dto.report;

import java.math.BigDecimal;

public record TopEventResponse(
	Long eventId, 
	String eventTitle, 
	long ticketsSold,
	BigDecimal revenue) {

	public static TopEventResponse from(TopEventProjection row) {
		return new TopEventResponse(row.getEventId(), row.getEventTitle(), row.getTicketsSold(), row.getRevenue());
	}

}

package com.example.base.dto.event;

import java.time.Instant;
import java.util.List;

import com.example.base.domain.Event;
import com.example.base.dto.category.CategoryResponse;
import com.example.base.dto.ticket.TicketTypeResponse;
import com.example.base.dto.venue.VenueResponse;

public record EventDetailResponse(Long id, String title, String description, String bannerUrl,
		Instant salesStartTime, Instant startTime, Instant endTime, CategoryResponse category, VenueResponse venue,
		List<TicketTypeResponse> ticketTypes) {

	public static EventDetailResponse from(Event event) {
		List<TicketTypeResponse> ticketTypes = event.getTicketTypes().stream()
				.map(TicketTypeResponse::from).toList();

		return new EventDetailResponse(event.getId(), event.getTitle(), event.getDescription(), event.getBannerUrl(),
				event.getSalesStartTime(), event.getStartTime(), event.getEndTime(),
				CategoryResponse.from(event.getCategory()), VenueResponse.from(event.getVenue()), ticketTypes);
	}

}

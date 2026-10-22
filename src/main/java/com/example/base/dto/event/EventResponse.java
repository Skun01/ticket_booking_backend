package com.example.base.dto.event;

import java.time.Instant;

import com.example.base.domain.Event;
import com.example.base.domain.enums.EventStatus;
import com.example.base.dto.category.CategoryResponse;
import com.example.base.dto.venue.VenueResponse;

public record EventResponse(Long id, Long organizerId, CategoryResponse category, VenueResponse venue,
		String title, String description, String bannerUrl, Instant startTime, Instant endTime,
		EventStatus status, Instant createdAt) {

	public static EventResponse from(Event event) {
		return new EventResponse(event.getId(), event.getOrganizer().getId(),
				CategoryResponse.from(event.getCategory()), VenueResponse.from(event.getVenue()),
				event.getTitle(), event.getDescription(), event.getBannerUrl(), event.getStartTime(),
				event.getEndTime(), event.getStatus(), event.getCreatedAt());
	}

}

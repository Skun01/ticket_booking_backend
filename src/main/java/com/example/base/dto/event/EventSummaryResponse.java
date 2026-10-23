package com.example.base.dto.event;

import java.time.Instant;

import com.example.base.domain.Event;
import com.example.base.dto.category.CategoryResponse;
import com.example.base.dto.venue.VenueResponse;

public record EventSummaryResponse(Long id, String title, String bannerUrl, Instant salesStartTime,
		Instant startTime, Instant endTime, CategoryResponse category, VenueResponse venue) {

	public static EventSummaryResponse from(Event event) {
		return new EventSummaryResponse(event.getId(), event.getTitle(), event.getBannerUrl(),
				event.getSalesStartTime(), event.getStartTime(), event.getEndTime(),
				CategoryResponse.from(event.getCategory()), VenueResponse.from(event.getVenue()));
	}

}

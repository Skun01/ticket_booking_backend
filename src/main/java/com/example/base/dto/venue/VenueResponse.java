package com.example.base.dto.venue;

import com.example.base.domain.Venue;

public record VenueResponse(Long id, String name, String address, String city, int capacity) {

	public static VenueResponse from(Venue venue) {
		return new VenueResponse(venue.getId(), venue.getName(), venue.getAddress(),
				venue.getCity(), venue.getCapacity());
	}

}

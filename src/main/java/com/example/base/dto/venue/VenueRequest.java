package com.example.base.dto.venue;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record VenueRequest(
		@NotBlank @Size(max = 150) String name,
		@NotBlank @Size(max = 255) String address,
		@NotBlank @Size(max = 100) String city,
		@Positive int capacity) {
}

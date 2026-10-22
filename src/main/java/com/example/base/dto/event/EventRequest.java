package com.example.base.dto.event;

import java.time.Instant;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record EventRequest(
		@NotNull @Positive Long categoryId,
		@NotNull @Positive Long venueId,
		@NotBlank @Size(max = 200) String title,
		@Size(max = 15000) String description,
		@Size(max = 500) String bannerUrl,
		@NotNull @Future Instant startTime,
		@NotNull Instant endTime) {
}

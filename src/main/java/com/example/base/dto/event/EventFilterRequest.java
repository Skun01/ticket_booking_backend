package com.example.base.dto.event;

import java.time.Instant;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;

public record EventFilterRequest(
		@Positive Long categoryId,
		@Size(max = 100) String city,
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
}

package com.example.base.repository.specification;

import java.time.Instant;
import java.util.Locale;

import com.example.base.domain.Event;
import com.example.base.domain.enums.EventStatus;

import org.springframework.data.jpa.domain.Specification;

public final class EventSpecifications {

	private EventSpecifications() {
	}

	public static Specification<Event> statusIs(EventStatus status) {
		return (root, query, builder) -> builder.equal(root.get("status"), status);
	}

	public static Specification<Event> categoryId(Long categoryId) {
		if (categoryId == null) {
			return Specification.unrestricted();
		}

		return (root, query, builder) -> builder.equal(root.get("category").get("id"), categoryId);
	}

	public static Specification<Event> city(String city) {
		if (city == null || city.isBlank()) {
			return Specification.unrestricted();
		}
		String normalizedCity = city.trim().toLowerCase(Locale.ROOT);

		return (root, query, builder) -> builder.equal(
				builder.lower(root.get("venue").get("city")), normalizedCity);
	}

	public static Specification<Event> keyword(String keyword) {
		if (keyword == null || keyword.isBlank()) {
			return Specification.unrestricted();
		}
		String pattern = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";

		return (root, query, builder) -> builder.or(
				builder.like(builder.lower(root.get("title")), pattern),
				builder.like(builder.lower(root.get("description")), pattern));
	}

	public static Specification<Event> startFrom(Instant from) {
		if (from == null) {
			return Specification.unrestricted();
		}

		return (root, query, builder) -> builder.greaterThanOrEqualTo(root.get("startTime"), from);
	}

	public static Specification<Event> startTo(Instant to) {
		if (to == null) {
			return Specification.unrestricted();
		}

		return (root, query, builder) -> builder.lessThan(root.get("startTime"), to);
	}

}

package com.example.base.service.impl;

import java.util.List;

import com.example.base.domain.Category;
import com.example.base.domain.Event;
import com.example.base.domain.TicketType;
import com.example.base.domain.User;
import com.example.base.domain.Venue;
import com.example.base.domain.enums.EventStatus;
import com.example.base.dto.common.PageResponse;
import com.example.base.dto.event.EventDetailResponse;
import com.example.base.dto.event.EventFilterRequest;
import com.example.base.dto.event.EventRequest;
import com.example.base.dto.event.EventResponse;
import com.example.base.dto.event.EventStatusRequest;
import com.example.base.dto.event.EventSummaryResponse;
import com.example.base.exception.AppException;
import com.example.base.exception.CatalogErrorCode;
import com.example.base.exception.EventErrorCode;
import com.example.base.exception.UserErrorCode;
import com.example.base.repository.CategoryRepository;
import com.example.base.repository.EventRepository;
import com.example.base.repository.TicketTypeRepository;
import com.example.base.repository.UserRepository;
import com.example.base.repository.VenueRepository;
import com.example.base.repository.specification.EventSpecifications;
import com.example.base.service.EventService;
import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

	private final EventRepository eventRepository;
	private final CategoryRepository categoryRepository;
	private final VenueRepository venueRepository;
	private final UserRepository userRepository;
	private final TicketTypeRepository ticketTypeRepository;

	@Override
	public PageResponse<EventSummaryResponse> listPublic(EventFilterRequest filter, Pageable pageable) {
		if (filter.from() != null && filter.to() != null && !filter.from().isBefore(filter.to())) {
			throw new AppException(EventErrorCode.INVALID_EVENT_FILTER_TIME);
		}
		Specification<Event> specification = Specification.allOf(
				EventSpecifications.statusIs(EventStatus.PUBLISHED),
				EventSpecifications.categoryId(filter.categoryId()),
				EventSpecifications.city(filter.city()),
				EventSpecifications.keyword(filter.keyword()),
				EventSpecifications.startFrom(filter.from()),
				EventSpecifications.startTo(filter.to()));

		return PageResponse.from(eventRepository.findAll(specification, pageable).map(EventSummaryResponse::from));
	}

	@Override
	@Cacheable(value = "event-detail", key = "#eventId")
	public EventDetailResponse getPublic(Long eventId) {
		Event event = eventRepository.findByIdAndStatus(eventId, EventStatus.PUBLISHED)
				.orElseThrow(() -> new AppException(EventErrorCode.EVENT_NOT_FOUND));

		return EventDetailResponse.from(event);
	}

	@Override
	public PageResponse<EventResponse> listOwn(Long organizerId, Pageable pageable) {
		return PageResponse.from(eventRepository.findByOrganizerId(organizerId, pageable).map(EventResponse::from));
	}

	@Override
	public EventResponse getOwn(Long organizerId, Long eventId) {
		Event event = eventRepository.findById(eventId)
				.orElseThrow(() -> new AppException(EventErrorCode.EVENT_NOT_FOUND));
		requireOwner(event, organizerId);

		return EventResponse.from(event);
	}

	@Override
	@Transactional
	public EventResponse create(Long organizerId, EventRequest request) {
		User organizer = userRepository.findById(organizerId)
				.orElseThrow(() -> new AppException(UserErrorCode.USER_NOT_FOUND));
		Category category = findCategory(request.categoryId());
		Venue venue = findLockedVenue(request.venueId());
		Event event = new Event(organizer, category, venue, request.title().trim(), request.description(),
				request.bannerUrl(), request.startTime(), request.endTime());
		eventRepository.saveAndFlush(event);

		return EventResponse.from(event);
	}

	@Override
	@Transactional
	@CacheEvict(value = "event-detail", key = "#eventId")
	public EventResponse update(Long organizerId, Long eventId, EventRequest request) {
		Event event = findLockedOwnEvent(organizerId, eventId);
		event.requireDraft();
		Category category = findCategory(request.categoryId());
		Venue venue = findLockedVenue(request.venueId());
		List<TicketType> ticketTypes = ticketTypeRepository.findByEventIdOrderByIdAsc(eventId);
		long totalQuantity = ticketTypes.stream().mapToLong(TicketType::getTotalQuantity).sum();
		if (totalQuantity > venue.getCapacity()) {
			throw new AppException(EventErrorCode.VENUE_CAPACITY_EXCEEDED);
		}
		ticketTypes.forEach(ticketType -> ticketType.validateSaleForEventStart(request.startTime()));
		event.update(category, venue, request.title().trim(), request.description(), request.bannerUrl(),
				request.startTime(), request.endTime());

		return EventResponse.from(event);
	}

	@Override
	@Transactional
	@CacheEvict(value = "event-detail", key = "#eventId")
	public EventResponse changeStatus(Long organizerId, Long eventId, EventStatusRequest request) {
		Event event = findLockedOwnEvent(organizerId, eventId);
		switch (request.status()) {
			case PUBLISHED -> {
				if (!ticketTypeRepository.existsByEventId(eventId)) {
					throw new AppException(EventErrorCode.EVENT_HAS_NO_TICKET_TYPES);
				}
				event.publish();
			}
			case CANCELLED -> event.cancel();
			case ENDED -> event.end();
			default -> throw new AppException(EventErrorCode.INVALID_STATUS_TRANSITION);
		}

		return EventResponse.from(event);
	}

	private Event findLockedOwnEvent(Long organizerId, Long eventId) {
		Event event = eventRepository.findLockedById(eventId)
				.orElseThrow(() -> new AppException(EventErrorCode.EVENT_NOT_FOUND));
		requireOwner(event, organizerId);

		return event;
	}

	private void requireOwner(Event event, Long organizerId) {
		if (!event.isOwnedBy(organizerId)) {
			throw new AppException(EventErrorCode.EVENT_NOT_OWNED);
		}
	}

	private Category findCategory(Long id) {
		return categoryRepository.findById(id)
				.orElseThrow(() -> new AppException(CatalogErrorCode.CATEGORY_NOT_FOUND));
	}

	private Venue findLockedVenue(Long id) {
		return venueRepository.findLockedById(id)
				.orElseThrow(() -> new AppException(CatalogErrorCode.VENUE_NOT_FOUND));
	}

}

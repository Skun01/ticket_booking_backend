package com.example.base.service.impl;

import java.util.List;

import com.example.base.domain.Event;
import com.example.base.domain.TicketType;
import com.example.base.dto.ticket.TicketTypeRequest;
import com.example.base.dto.ticket.TicketTypeResponse;
import com.example.base.exception.AppException;
import com.example.base.exception.EventErrorCode;
import com.example.base.exception.TicketTypeErrorCode;
import com.example.base.repository.EventRepository;
import com.example.base.repository.TicketTypeRepository;
import com.example.base.service.TicketTypeService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketTypeServiceImpl implements TicketTypeService {

	private final TicketTypeRepository ticketTypeRepository;
	private final EventRepository eventRepository;

	@Override
	public List<TicketTypeResponse> listOwn(Long organizerId, Long eventId) {
		Event event = eventRepository.findById(eventId)
				.orElseThrow(() -> new AppException(EventErrorCode.EVENT_NOT_FOUND));
		requireOwner(event, organizerId);

		return ticketTypeRepository.findByEventIdOrderByIdAsc(eventId).stream()
				.map(TicketTypeResponse::from).toList();
	}

	@Override
	@Transactional
	public TicketTypeResponse create(Long organizerId, Long eventId, TicketTypeRequest request) {
		Event event = findLockedOwnEvent(organizerId, eventId);
		validateQuantity(event, null, request);
		TicketType ticketType = new TicketType(event, request.name().trim(), request.price(), request.totalQuantity(),
				request.maxPerOrder());
		ticketTypeRepository.saveAndFlush(ticketType);

		return TicketTypeResponse.from(ticketType);
	}

	@Override
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public TicketTypeResponse update(Long organizerId, Long ticketTypeId, TicketTypeRequest request) {
		Long eventId = ticketTypeRepository.findEventIdById(ticketTypeId)
				.orElseThrow(() -> new AppException(TicketTypeErrorCode.TICKET_TYPE_NOT_FOUND));
		// Khóa event trước ticket type, cùng thứ tự với luồng giữ kho để tránh deadlock.
		Event event = findLockedOwnEvent(organizerId, eventId);
		TicketType ticketType = ticketTypeRepository.findLockedById(ticketTypeId)
				.orElseThrow(() -> new AppException(TicketTypeErrorCode.TICKET_TYPE_NOT_FOUND));
		validateQuantity(event, ticketTypeId, request);
		ticketType.update(request.name().trim(), request.price(), request.totalQuantity(), request.maxPerOrder());

		return TicketTypeResponse.from(ticketType);
	}

	private Event findLockedOwnEvent(Long organizerId, Long eventId) {
		// Khóa sự kiện để hai thao tác cấu hình không cùng vượt sức chứa địa điểm.
		Event event = eventRepository.findLockedById(eventId)
				.orElseThrow(() -> new AppException(EventErrorCode.EVENT_NOT_FOUND));
		requireOwner(event, organizerId);
		event.requireDraft();

		return event;
	}

	private void requireOwner(Event event, Long organizerId) {
		if (!event.isOwnedBy(organizerId)) {
			throw new AppException(EventErrorCode.EVENT_NOT_OWNED);
		}
	}

	private void validateQuantity(Event event, Long excludedId, TicketTypeRequest request) {
		if (request.maxPerOrder() > request.totalQuantity()) {
			throw new AppException(TicketTypeErrorCode.INVALID_TICKET_QUANTITY);
		}
		long otherQuantity = ticketTypeRepository.findByEventIdOrderByIdAsc(event.getId()).stream()
				.filter(ticketType -> !ticketType.getId().equals(excludedId))
				.mapToLong(TicketType::getTotalQuantity).sum();
		if (otherQuantity + request.totalQuantity() > event.getVenue().getCapacity()) {
			throw new AppException(EventErrorCode.VENUE_CAPACITY_EXCEEDED);
		}
	}

}

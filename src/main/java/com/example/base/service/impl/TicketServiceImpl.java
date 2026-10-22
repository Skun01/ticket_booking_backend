package com.example.base.service.impl;

import java.time.Instant;

import com.example.base.domain.Event;
import com.example.base.domain.Ticket;
import com.example.base.domain.enums.EventStatus;
import com.example.base.domain.enums.OrderStatus;
import com.example.base.dto.common.PageResponse;
import com.example.base.dto.ticket.TicketResponse;
import com.example.base.exception.AppException;
import com.example.base.exception.BookingTicketErrorCode;
import com.example.base.repository.EventRepository;
import com.example.base.repository.TicketRepository;
import com.example.base.service.TicketService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketServiceImpl implements TicketService {

	private final TicketRepository ticketRepository;
	private final EventRepository eventRepository;

	@Override
	public PageResponse<TicketResponse> listOwn(Long customerId, Pageable pageable) {
		return PageResponse.from(ticketRepository.findByOrderItemOrderUserId(customerId, pageable)
				.map(TicketResponse::from));
	}

	@Override
	@Transactional
	public TicketResponse checkIn(Long organizerId, String ticketCode) {
		Ticket ticket = ticketRepository.findLockedByTicketCode(ticketCode.trim())
				.orElseThrow(() -> new AppException(BookingTicketErrorCode.TICKET_NOT_FOUND));
		Long eventId = ticket.getOrderItem().getTicketType().getEvent().getId();
		Event event = eventRepository.findBookingById(eventId)
				.orElseThrow(() -> new AppException(BookingTicketErrorCode.TICKET_NOT_FOUND));
		if (!event.isOwnedBy(organizerId)) {
			throw new AppException(BookingTicketErrorCode.TICKET_NOT_OWNED_BY_ORGANIZER);
		}
		Instant now = Instant.now();
		if (event.getStatus() != EventStatus.PUBLISHED || !now.isBefore(event.getEndTime())) {
			throw new AppException(BookingTicketErrorCode.EVENT_NOT_OPEN_FOR_CHECKIN);
		}
		if (ticket.getOrderItem().getOrder().getStatus() != OrderStatus.PAID) {
			throw new AppException(BookingTicketErrorCode.TICKET_NOT_VALID);
		}
		ticket.use(now);

		return TicketResponse.from(ticket);
	}

}

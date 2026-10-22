package com.example.base.service;

import java.util.List;

import com.example.base.dto.ticket.TicketTypeRequest;
import com.example.base.dto.ticket.TicketTypeResponse;

public interface TicketTypeService {

	List<TicketTypeResponse> listOwn(Long organizerId, Long eventId);

	TicketTypeResponse create(Long organizerId, Long eventId, TicketTypeRequest request);

	TicketTypeResponse update(Long organizerId, Long ticketTypeId, TicketTypeRequest request);

}

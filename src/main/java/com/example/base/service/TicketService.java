package com.example.base.service;

import com.example.base.dto.common.PageResponse;
import com.example.base.dto.ticket.TicketResponse;

import org.springframework.data.domain.Pageable;

public interface TicketService {

	PageResponse<TicketResponse> listOwn(Long customerId, Pageable pageable);

	TicketResponse checkIn(Long organizerId, String ticketCode);

}

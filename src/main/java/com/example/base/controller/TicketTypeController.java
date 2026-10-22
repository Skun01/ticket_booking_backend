package com.example.base.controller;

import com.example.base.annotation.CurrentUser;
import com.example.base.annotation.IsOrganizer;
import com.example.base.dto.ticket.TicketTypeRequest;
import com.example.base.dto.ticket.TicketTypeResponse;
import com.example.base.service.TicketTypeService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ticket-types")
@IsOrganizer
@RequiredArgsConstructor
public class TicketTypeController {

	private final TicketTypeService ticketTypeService;

	@Operation(summary = "Cập nhật hạng vé")
	@PutMapping("/{id}")
	public TicketTypeResponse update(@CurrentUser Jwt jwt, @PathVariable Long id,
			@Valid @RequestBody TicketTypeRequest request) {
		return ticketTypeService.update(Long.valueOf(jwt.getSubject()), id, request);
	}

}

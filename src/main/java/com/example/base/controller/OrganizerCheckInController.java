package com.example.base.controller;

import com.example.base.annotation.CurrentUser;
import com.example.base.annotation.IsOrganizer;
import com.example.base.dto.ticket.CheckInRequest;
import com.example.base.dto.ticket.TicketResponse;
import com.example.base.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organizer/checkin")
@IsOrganizer
@RequiredArgsConstructor
public class OrganizerCheckInController {

	private final TicketService ticketService;

	@Operation(summary = "Check-in vé cho sự kiện của tôi")
	@PostMapping
	public TicketResponse checkIn(@CurrentUser Jwt jwt, @Valid @RequestBody CheckInRequest request) {
		return ticketService.checkIn(Long.valueOf(jwt.getSubject()), request.ticketCode());
	}

}

package com.example.base.controller;

import com.example.base.annotation.CurrentUser;
import com.example.base.annotation.IsCustomer;
import com.example.base.dto.common.PageResponse;
import com.example.base.dto.ticket.TicketResponse;
import com.example.base.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
@IsCustomer
@RequiredArgsConstructor
public class TicketController {

	private final TicketService ticketService;

	@Operation(summary = "Danh sách vé của tôi")
	@GetMapping("/my")
	public PageResponse<TicketResponse> listOwn(@CurrentUser Jwt jwt,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
		return ticketService.listOwn(Long.valueOf(jwt.getSubject()),
				PageRequest.of(page, size, Sort.by("id").descending()));
	}

}

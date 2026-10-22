package com.example.base.controller;

import com.example.base.dto.common.PageResponse;
import com.example.base.dto.event.EventDetailResponse;
import com.example.base.dto.event.EventFilterRequest;
import com.example.base.dto.event.EventSummaryResponse;
import com.example.base.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
@SecurityRequirements
@RequiredArgsConstructor
public class EventController {

	private final EventService eventService;

	@Operation(summary = "Tìm kiếm sự kiện công khai")
	@GetMapping
	public PageResponse<EventSummaryResponse> list(@Valid @ModelAttribute EventFilterRequest filter,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
		return eventService.listPublic(filter, PageRequest.of(page, size, Sort.by("startTime", "id")));
	}

	@Operation(summary = "Chi tiết sự kiện công khai")
	@GetMapping("/{id}")
	public EventDetailResponse getById(@PathVariable @Positive Long id) {
		return eventService.getPublic(id);
	}

}

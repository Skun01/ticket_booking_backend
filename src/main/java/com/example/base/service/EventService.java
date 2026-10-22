package com.example.base.service;

import com.example.base.dto.common.PageResponse;
import com.example.base.dto.event.EventRequest;
import com.example.base.dto.event.EventResponse;
import com.example.base.dto.event.EventStatusRequest;

import org.springframework.data.domain.Pageable;

public interface EventService {

	PageResponse<EventResponse> listOwn(Long organizerId, Pageable pageable);

	EventResponse getOwn(Long organizerId, Long eventId);

	EventResponse create(Long organizerId, EventRequest request);

	EventResponse update(Long organizerId, Long eventId, EventRequest request);

	EventResponse changeStatus(Long organizerId, Long eventId, EventStatusRequest request);

}

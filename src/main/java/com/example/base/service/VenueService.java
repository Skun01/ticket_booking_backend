package com.example.base.service;

import com.example.base.dto.common.PageResponse;
import com.example.base.dto.venue.VenueRequest;
import com.example.base.dto.venue.VenueResponse;

import org.springframework.data.domain.Pageable;

public interface VenueService {

	PageResponse<VenueResponse> list(Pageable pageable);

	VenueResponse getById(Long id);

	VenueResponse create(VenueRequest request);

	VenueResponse update(Long id, VenueRequest request);

	void delete(Long id);

}

package com.example.base.service.impl;

import com.example.base.domain.Venue;
import com.example.base.dto.common.PageResponse;
import com.example.base.dto.venue.VenueRequest;
import com.example.base.dto.venue.VenueResponse;
import com.example.base.exception.AppException;
import com.example.base.exception.CatalogErrorCode;
import com.example.base.repository.EventRepository;
import com.example.base.repository.VenueRepository;
import com.example.base.service.VenueService;
import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VenueServiceImpl implements VenueService {

	private final VenueRepository venueRepository;
	private final EventRepository eventRepository;

	@Override
	public PageResponse<VenueResponse> list(Pageable pageable) {
		return PageResponse.from(venueRepository.findAll(pageable).map(VenueResponse::from));
	}

	@Override
	public VenueResponse getById(Long id) {
		Venue venue = venueRepository.findById(id)
				.orElseThrow(() -> new AppException(CatalogErrorCode.VENUE_NOT_FOUND));

		return VenueResponse.from(venue);
	}

	@Override
	@Transactional
	public VenueResponse create(VenueRequest request) {
		Venue venue = new Venue(request.name().trim(), request.address().trim(),
				request.city().trim(), request.capacity());
		venueRepository.saveAndFlush(venue);

		return VenueResponse.from(venue);
	}

	@Override
	@Transactional
	public VenueResponse update(Long id, VenueRequest request) {
		Venue venue = findLockedVenue(id);
		if (venue.getCapacity() != request.capacity() && eventRepository.existsByVenueId(id)) {
			throw new AppException(CatalogErrorCode.VENUE_CAPACITY_IN_USE);
		}
		venue.update(request.name().trim(), request.address().trim(), request.city().trim(), request.capacity());

		return VenueResponse.from(venue);
	}

	@Override
	@Transactional
	public void delete(Long id) {
		Venue venue = findLockedVenue(id);
		if (eventRepository.existsByVenueId(id)) {
			throw new AppException(CatalogErrorCode.VENUE_IN_USE);
		}
		try {
			venueRepository.delete(venue);
			venueRepository.flush();
		}
		catch (DataIntegrityViolationException ex) {
			throw new AppException(CatalogErrorCode.VENUE_IN_USE);
		}
	}

	private Venue findLockedVenue(Long id) {
		return venueRepository.findLockedById(id)
				.orElseThrow(() -> new AppException(CatalogErrorCode.VENUE_NOT_FOUND));
	}

}

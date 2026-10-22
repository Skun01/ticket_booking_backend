package com.example.base.controller;

import java.net.URI;

import com.example.base.annotation.IsAdmin;
import com.example.base.dto.venue.VenueRequest;
import com.example.base.dto.venue.VenueResponse;
import com.example.base.dto.common.PageResponse;
import com.example.base.service.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/venues")
@IsAdmin
@RequiredArgsConstructor
public class AdminVenueController {

	private final VenueService venueService;

	@Operation(summary = "Danh sách địa điểm")
	@GetMapping
	public PageResponse<VenueResponse> list(
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
		return venueService.list(PageRequest.of(page, size, Sort.by("id").descending()));
	}

	@Operation(summary = "Chi tiết địa điểm")
	@GetMapping("/{id}")
	public VenueResponse getById(@PathVariable Long id) {
		return venueService.getById(id);
	}

	@Operation(summary = "Tạo địa điểm")
	@PostMapping
	public ResponseEntity<VenueResponse> create(@Valid @RequestBody VenueRequest request) {
		VenueResponse response = venueService.create(request);

		return ResponseEntity.created(URI.create("/api/admin/venues/" + response.id())).body(response);
	}

	@Operation(summary = "Cập nhật địa điểm")
	@PutMapping("/{id}")
	public VenueResponse update(@PathVariable Long id, @Valid @RequestBody VenueRequest request) {
		return venueService.update(id, request);
	}

	@Operation(summary = "Xóa địa điểm")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		venueService.delete(id);

		return ResponseEntity.noContent().build();
	}

}

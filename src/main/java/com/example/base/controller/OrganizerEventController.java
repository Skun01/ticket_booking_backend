package com.example.base.controller;

import java.net.URI;
import java.util.List;

import com.example.base.annotation.CurrentUser;
import com.example.base.annotation.IsOrganizer;
import com.example.base.dto.common.PageResponse;
import com.example.base.dto.event.EventRequest;
import com.example.base.dto.event.EventResponse;
import com.example.base.dto.event.EventStatusRequest;
import com.example.base.dto.report.EventReportResponse;
import com.example.base.dto.ticket.TicketTypeRequest;
import com.example.base.dto.ticket.TicketTypeResponse;
import com.example.base.service.EventService;
import com.example.base.service.ReportService;
import com.example.base.service.TicketTypeService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organizer/events")
@IsOrganizer
@RequiredArgsConstructor
public class OrganizerEventController {

	private final EventService eventService;
	private final TicketTypeService ticketTypeService;
	private final ReportService reportService;

	@Operation(summary = "Danh sách sự kiện của tôi")
	@GetMapping
	public PageResponse<EventResponse> list(@CurrentUser Jwt jwt,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
		return eventService.listOwn(Long.valueOf(jwt.getSubject()),
				PageRequest.of(page, size, Sort.by("id").descending()));
	}

	@Operation(summary = "Chi tiết sự kiện của tôi")
	@GetMapping("/{id}")
	public EventResponse getById(@CurrentUser Jwt jwt, @PathVariable Long id) {
		return eventService.getOwn(Long.valueOf(jwt.getSubject()), id);
	}

	@Operation(summary = "Báo cáo doanh thu và số vé bán của sự kiện")
	@GetMapping("/{id}/report")
	public EventReportResponse report(@CurrentUser Jwt jwt, @PathVariable Long id) {
		return reportService.getEventReport(Long.valueOf(jwt.getSubject()), id);
	}

	@Operation(summary = "Tải báo cáo doanh thu sự kiện dạng PDF")
	@GetMapping(value = "/{id}/report/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<byte[]> reportPdf(@CurrentUser Jwt jwt, @PathVariable Long id) {
		byte[] pdf = reportService.getEventReportPdf(Long.valueOf(jwt.getSubject()), id);

		return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).cacheControl(CacheControl.noStore())
				.header(HttpHeaders.CONTENT_DISPOSITION,
						ContentDisposition.attachment().filename("event-" + id + "-revenue.pdf").build().toString())
				.contentLength(pdf.length).body(pdf);
	}

	@Operation(summary = "Tạo sự kiện")
	@PostMapping
	public ResponseEntity<EventResponse> create(@CurrentUser Jwt jwt, @Valid @RequestBody EventRequest request) {
		EventResponse response = eventService.create(Long.valueOf(jwt.getSubject()), request);

		return ResponseEntity.created(URI.create("/api/organizer/events/" + response.id())).body(response);
	}

	@Operation(summary = "Cập nhật sự kiện")
	@PutMapping("/{id}")
	public EventResponse update(@CurrentUser Jwt jwt, @PathVariable Long id,
			@Valid @RequestBody EventRequest request) {
		return eventService.update(Long.valueOf(jwt.getSubject()), id, request);
	}

	@Operation(summary = "Đổi trạng thái sự kiện")
	@PatchMapping("/{id}/status")
	public EventResponse changeStatus(@CurrentUser Jwt jwt, @PathVariable Long id,
			@Valid @RequestBody EventStatusRequest request) {
		return eventService.changeStatus(Long.valueOf(jwt.getSubject()), id, request);
	}

	@Operation(summary = "Danh sách hạng vé của sự kiện")
	@GetMapping("/{id}/ticket-types")
	public List<TicketTypeResponse> listTicketTypes(@CurrentUser Jwt jwt, @PathVariable Long id) {
		return ticketTypeService.listOwn(Long.valueOf(jwt.getSubject()), id);
	}

	@Operation(summary = "Tạo hạng vé cho sự kiện")
	@PostMapping("/{id}/ticket-types")
	public ResponseEntity<TicketTypeResponse> createTicketType(@CurrentUser Jwt jwt, @PathVariable Long id,
			@Valid @RequestBody TicketTypeRequest request) {
		TicketTypeResponse response = ticketTypeService.create(Long.valueOf(jwt.getSubject()), id, request);

		return ResponseEntity.created(URI.create("/api/organizer/events/" + id + "/ticket-types")).body(response);
	}

}

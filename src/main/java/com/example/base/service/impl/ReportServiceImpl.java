package com.example.base.service.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.example.base.domain.Event;
import com.example.base.dto.report.DailyRevenueResponse;
import com.example.base.dto.report.EventReportResponse;
import com.example.base.dto.report.EventTotalsProjection;
import com.example.base.dto.report.RevenueReportResponse;
import com.example.base.dto.report.TopEventResponse;
import com.example.base.exception.AppException;
import com.example.base.exception.EventErrorCode;
import com.example.base.exception.ReportErrorCode;
import com.example.base.repository.EventRepository;
import com.example.base.repository.PaymentRepository;
import com.example.base.service.ReportService;
import com.example.base.util.JasperPdfRenderer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

	private final EventRepository eventRepository;
	private final PaymentRepository paymentRepository;
	private final EntityManager entityManager;
	private final JasperPdfRenderer pdfRenderer;

	@Override
	public EventReportResponse getEventReport(Long organizerId, Long eventId) {
		Event event = eventRepository.findById(eventId)
				.orElseThrow(() -> new AppException(EventErrorCode.EVENT_NOT_FOUND));
		if (!event.isOwnedBy(organizerId)) {
			throw new AppException(EventErrorCode.EVENT_NOT_OWNED);
		}
		EventTotalsProjection totals = paymentRepository.findTotalsByEventId(eventId);
		List<DailyRevenueResponse> dailyRevenue = paymentRepository.findDailyRevenueByEventId(eventId).stream()
				.map(DailyRevenueResponse::from)
				.toList();

		return new EventReportResponse(event.getId(), event.getTitle(), totals.getPaidOrders(),
				totals.getTicketsSold(), totals.getRevenue(), dailyRevenue);
	}

	@Override
	public byte[] getEventReportPdf(Long organizerId, Long eventId) {
		return pdfRenderer.revenue(getEventReport(organizerId, eventId));
	}

	@Override
	public RevenueReportResponse getRevenueReport(Instant from, Instant to) {
		if (from == null || to == null || !from.isBefore(to)) {
			throw new AppException(ReportErrorCode.INVALID_REPORT_RANGE);
		}

		// call procedure 
		StoredProcedureQuery procedure = entityManager.createStoredProcedureQuery("sp_revenue_report");
		procedure.registerStoredProcedureParameter(1, Instant.class, ParameterMode.IN);
		procedure.registerStoredProcedureParameter(2, Instant.class, ParameterMode.IN);
		procedure.setParameter(1, from);
		procedure.setParameter(2, to);

		List<?> rows = procedure.getResultList();
		List<DailyRevenueResponse> dailyRevenue = rows.stream()
				.map(row -> (Object[]) row)
				.map(DailyRevenueResponse::from).toList();

		List<TopEventResponse> topEvents = paymentRepository.findTopEvents(from, to).stream()
				.map(TopEventResponse::from)
				.toList();

		long paidOrders = dailyRevenue.stream()
				.mapToLong(DailyRevenueResponse::paidOrders)
				.sum();

		BigDecimal revenue = dailyRevenue.stream()
				.map(DailyRevenueResponse::revenue)
				.reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);

		return new RevenueReportResponse(from, to, paidOrders, revenue, dailyRevenue, topEvents);
	}

}

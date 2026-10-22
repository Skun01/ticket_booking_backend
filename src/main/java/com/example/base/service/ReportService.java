package com.example.base.service;

import java.time.Instant;

import com.example.base.dto.report.EventReportResponse;
import com.example.base.dto.report.RevenueReportResponse;

public interface ReportService {

	EventReportResponse getEventReport(Long organizerId, Long eventId);

	RevenueReportResponse getRevenueReport(Instant from, Instant to);

}

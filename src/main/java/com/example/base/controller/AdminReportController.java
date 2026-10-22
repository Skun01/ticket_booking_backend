package com.example.base.controller;

import java.time.Instant;

import com.example.base.annotation.IsAdmin;
import com.example.base.dto.report.RevenueReportResponse;
import com.example.base.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reports")
@IsAdmin
@RequiredArgsConstructor
public class AdminReportController {

	private final ReportService reportService;

	@Operation(summary = "Báo cáo doanh thu theo khoảng thời gian")
	@GetMapping("/revenue")
	public RevenueReportResponse revenue(@RequestParam Instant from, @RequestParam Instant to) {
		return reportService.getRevenueReport(from, to);
	}

}

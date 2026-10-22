package com.example.base.dto.report;

import java.math.BigDecimal;
import java.util.List;

public record EventReportResponse(
		Long eventId,
		String eventTitle,
		long paidOrders,
		long ticketsSold,
		BigDecimal revenue,
		List<DailyRevenueResponse> dailyRevenue
) {
}

package com.example.base.dto.report;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record RevenueReportResponse(
		Instant from,
		Instant to,
		long paidOrders,
		BigDecimal revenue,
		List<DailyRevenueResponse> dailyRevenue,
		List<TopEventResponse> topEvents
) {
}

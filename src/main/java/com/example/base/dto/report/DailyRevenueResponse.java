package com.example.base.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyRevenueResponse(LocalDate date, long paidOrders, BigDecimal revenue) {

	public static DailyRevenueResponse from(DailyRevenueProjection row) {
		return new DailyRevenueResponse(row.getRevenueDate(), row.getPaidOrders(), row.getRevenue());
	}

	public static DailyRevenueResponse from(Object[] row) {
		return new DailyRevenueResponse(LocalDate.parse(row[0].toString()),
				((Number) row[1]).longValue(), (BigDecimal) row[2]);
	}

}

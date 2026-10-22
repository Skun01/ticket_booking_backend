package com.example.base.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DailyRevenueProjection {

	LocalDate getRevenueDate();

	long getPaidOrders();

	BigDecimal getRevenue();

}

package com.example.base.dto.report;

import java.math.BigDecimal;

public interface EventTotalsProjection {

	long getPaidOrders();

	long getTicketsSold();

	BigDecimal getRevenue();

}

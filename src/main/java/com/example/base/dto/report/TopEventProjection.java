package com.example.base.dto.report;

import java.math.BigDecimal;

public interface TopEventProjection {

	Long getEventId();

	String getEventTitle();

	long getTicketsSold();

	BigDecimal getRevenue();

}

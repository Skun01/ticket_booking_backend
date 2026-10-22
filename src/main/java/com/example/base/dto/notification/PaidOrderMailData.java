package com.example.base.dto.notification;

import java.math.BigDecimal;
import java.util.List;

public record PaidOrderMailData(String recipient, String fullName, String orderCode, BigDecimal totalAmount,
		List<TicketMailData> tickets) {
}

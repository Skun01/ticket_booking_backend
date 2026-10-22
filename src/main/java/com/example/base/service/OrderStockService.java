package com.example.base.service;

import com.example.base.domain.Order;
import com.example.base.domain.TicketType;

public interface OrderStockService {

	TicketType reserve(Long ticketTypeId, int quantity);

	void release(Order order);

}

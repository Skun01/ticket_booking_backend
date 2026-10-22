package com.example.base.service;

import com.example.base.dto.order.OrderMessage;

public interface ActivityLogService {

	void record(OrderMessage message);

}

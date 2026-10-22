package com.example.base.service;

import com.example.base.dto.notification.PaidOrderMailData;
import com.example.base.dto.order.OrderMessage;

public interface OrderMailService {

	PaidOrderMailData loadPaidOrder(OrderMessage message);

}

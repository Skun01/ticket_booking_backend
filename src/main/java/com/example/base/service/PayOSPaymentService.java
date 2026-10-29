package com.example.base.service;

import com.example.base.dto.order.OrderResponse;
import com.example.base.dto.payment.PayOSCheckoutResponse;
import vn.payos.model.webhooks.Webhook;

public interface PayOSPaymentService {

	PayOSCheckoutResponse createPaymentLink(Long customerId, Long orderId);

	void handleWebhook(Webhook webhook);

	OrderResponse syncPayment(Long customerId, Long orderId);

}

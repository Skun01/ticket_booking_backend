package com.example.base.controller;

import com.example.base.dto.payment.PayOSWebhookResponse;
import com.example.base.service.PayOSPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import vn.payos.model.webhooks.Webhook;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

	private final PayOSPaymentService payOSPaymentService;

	@Operation(summary = "Tiếp nhận webhook thanh toán từ PayOS")
	@PostMapping("/payos/webhook")
	public PayOSWebhookResponse handlePayOSWebhook(@RequestBody Webhook webhook) {
		payOSPaymentService.handleWebhook(webhook);

		return PayOSWebhookResponse.success();
	}

}

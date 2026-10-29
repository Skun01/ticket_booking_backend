package com.example.base.dto.payment;

public record PayOSWebhookResponse(
		String code,
		String message) {

	public static PayOSWebhookResponse success() {
		return new PayOSWebhookResponse("00", "Webhook processed successfully");
	}

}

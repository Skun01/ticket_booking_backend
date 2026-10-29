package com.example.base.dto.payment;

public record PayOSCheckoutResponse(
		Long orderId,
		Long orderCode,
		String checkoutUrl,
		String qrCode,
		String paymentLinkId,
		Long amount,
		String status) {
}

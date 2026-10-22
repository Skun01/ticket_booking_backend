package com.example.base.dto.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.example.base.domain.Order;
import com.example.base.domain.enums.OrderStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record OrderMessage(
		@NotBlank @Pattern(regexp = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}") String messageId,
		@NotNull @Positive Long orderId,
		@NotBlank @Size(max = 40) String orderCode,
		@NotNull @Positive Long userId,
		@NotBlank @Email @Size(max = 255) String email,
		@NotNull OrderStatus status,
		@NotNull @DecimalMin("0.00") @Digits(integer = 14, fraction = 2) BigDecimal totalAmount,
		@NotNull Instant occurredAt) {

	public static OrderMessage from(Order order, Instant occurredAt) {
		return new OrderMessage(UUID.randomUUID().toString(), order.getId(), order.getOrderCode(),
				order.getUser().getId(), order.getUser().getEmail(), order.getStatus(),
				order.getTotalAmount(), occurredAt);
	}

}

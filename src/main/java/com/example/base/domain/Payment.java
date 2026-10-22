package com.example.base.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.example.base.domain.enums.PaymentMethod;
import com.example.base.domain.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private PaymentMethod method;

	@Column(nullable = false, precision = 16, scale = 2)
	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private PaymentStatus status;

	@Column(nullable = false, unique = true, length = 40)
	private String transactionRef;

	private Instant paidAt;

	private Payment(Order order, PaymentMethod method, BigDecimal amount, PaymentStatus status, Instant paidAt) {
		this.order = order;
		this.method = method;
		this.amount = amount;
		this.status = status;
		this.transactionRef = "PAY-" + UUID.randomUUID();
		this.paidAt = paidAt;
	}

	public static Payment success(Order order, PaymentMethod method, Instant paidAt) {
		return new Payment(order, method, order.getTotalAmount(), PaymentStatus.SUCCESS, paidAt);
	}

	public static Payment failed(Order order, PaymentMethod method, BigDecimal amount) {
		return new Payment(order, method, amount, PaymentStatus.FAILED, null);
	}

}

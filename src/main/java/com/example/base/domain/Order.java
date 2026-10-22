package com.example.base.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.base.domain.enums.OrderStatus;
import com.example.base.exception.AppException;
import com.example.base.exception.OrderErrorCode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

	@Column(nullable = false, unique = true, length = 40)
	private String orderCode;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(length = 50)
	private String promoCode;

	@Column(nullable = false, precision = 16, scale = 2)
	private BigDecimal totalAmount;

	@Column(nullable = false, precision = 16, scale = 2)
	private BigDecimal discountAmount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private OrderStatus status;

	@Column(nullable = false)
	private Instant expiresAt;

	@OneToMany(mappedBy = "order", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
	@OrderBy("id ASC")
	@BatchSize(size = 50)
	private List<OrderItem> items = new ArrayList<>();

	public Order(User user, TicketType ticketType, int quantity, Instant expiresAt) {
		if (quantity < 1 || quantity > 100 || quantity > ticketType.getMaxPerOrder()) {
			throw new AppException(OrderErrorCode.INVALID_ORDER_QUANTITY);
		}
		this.orderCode = "ORD-" + UUID.randomUUID();
		this.user = user;
		this.status = OrderStatus.PENDING;
		this.expiresAt = expiresAt;
		this.discountAmount = BigDecimal.ZERO.setScale(2);
		this.totalAmount = ticketType.getPrice().multiply(BigDecimal.valueOf(quantity));
		this.items.add(new OrderItem(this, ticketType, quantity));
	}

	public boolean isOwnedBy(Long userId) {
		return user.getId().equals(userId);
	}

	public boolean isDue(Instant now) {
		return !expiresAt.isAfter(now);
	}

	public void requirePending() {
		if (status != OrderStatus.PENDING) {
			throw new AppException(OrderErrorCode.ORDER_NOT_PENDING);
		}
	}

	public void markPaid(Instant now) {
		requirePending();
		if (isDue(now)) {
			throw new AppException(OrderErrorCode.ORDER_EXPIRED);
		}
		status = OrderStatus.PAID;
	}

	public void cancel() {
		requirePending();
		status = OrderStatus.CANCELLED;
	}

	public boolean expire(Instant now) {
		if (status != OrderStatus.PENDING || !isDue(now)) {
			return false;
		}
		status = OrderStatus.EXPIRED;

		return true;
	}

}

package com.example.base.domain;

import java.math.BigDecimal;
import java.time.Instant;

import com.example.base.exception.AppException;
import com.example.base.exception.OrderErrorCode;
import com.example.base.exception.TicketTypeErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "ticket_types")
public class TicketType extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "event_id", nullable = false)
	private Event event;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal price;

	@Column(nullable = false)
	private int totalQuantity;

	@Column(nullable = false)
	private int soldQuantity;

	@Column(nullable = false)
	private int maxPerOrder;

	public TicketType(Event event, String name, BigDecimal price, int totalQuantity, int maxPerOrder) {
		this.event = event;
		update(name, price, totalQuantity, maxPerOrder);
	}

	public void update(String name, BigDecimal price, int totalQuantity, int maxPerOrder) {
		event.requireDraft();
		if (totalQuantity < soldQuantity) {
			throw new AppException(TicketTypeErrorCode.QUANTITY_BELOW_SOLD);
		}
		this.name = name;
		this.price = price;
		this.totalQuantity = totalQuantity;
		this.maxPerOrder = maxPerOrder;
	}

	public void requireBookable(int quantity, Instant now) {
		event.requireBookable(now);
		if (quantity < 1 || quantity > maxPerOrder) {
			throw new AppException(OrderErrorCode.INVALID_ORDER_QUANTITY);
		}
	}

	public int getAvailableQuantity() {
		return totalQuantity - soldQuantity;
	}

}

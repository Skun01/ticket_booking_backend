package com.example.base.domain;

import java.math.BigDecimal;
import java.time.Instant;

import com.example.base.exception.AppException;
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

	private Instant saleStart;

	private Instant saleEnd;

	public TicketType(Event event, String name, BigDecimal price, int totalQuantity,
			int maxPerOrder, Instant saleStart, Instant saleEnd) {
		this.event = event;
		update(name, price, totalQuantity, maxPerOrder, saleStart, saleEnd);
	}

	public void update(String name, BigDecimal price, int totalQuantity, int maxPerOrder,
			Instant saleStart, Instant saleEnd) {
		event.requireDraft();
		if (totalQuantity < soldQuantity) {
			throw new AppException(TicketTypeErrorCode.QUANTITY_BELOW_SOLD);
		}
		this.name = name;
		this.price = price;
		this.totalQuantity = totalQuantity;
		this.maxPerOrder = maxPerOrder;
		this.saleStart = saleStart;
		this.saleEnd = saleEnd;
		validateSaleForEventStart(event.getStartTime());
	}

	public void validateSaleForEventStart(Instant eventStart) {
		if (saleStart != null && !saleStart.isBefore(eventStart)
				|| saleEnd != null && saleEnd.isAfter(eventStart)
				|| saleStart != null && saleEnd != null && !saleEnd.isAfter(saleStart)) {
			throw new AppException(TicketTypeErrorCode.INVALID_SALE_TIME);
		}
	}

	public int getAvailableQuantity() {
		return totalQuantity - soldQuantity;
	}

}

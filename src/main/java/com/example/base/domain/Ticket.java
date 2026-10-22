package com.example.base.domain;

import java.time.Instant;
import java.util.UUID;

import com.example.base.domain.enums.TicketStatus;
import com.example.base.exception.AppException;
import com.example.base.exception.BookingTicketErrorCode;
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
@Table(name = "tickets")
public class Ticket extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "order_item_id", nullable = false)
	private OrderItem orderItem;

	@Column(nullable = false, unique = true, length = 40)
	private String ticketCode;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TicketStatus status;

	private Instant checkedInAt;

	public Ticket(OrderItem orderItem) {
		this.orderItem = orderItem;
		this.ticketCode = "TKT-" + UUID.randomUUID();
		this.status = TicketStatus.VALID;
	}

	public void use(Instant now) {
		if (status == TicketStatus.USED) {
			throw new AppException(BookingTicketErrorCode.TICKET_ALREADY_USED);
		}
		if (status != TicketStatus.VALID) {
			throw new AppException(BookingTicketErrorCode.TICKET_NOT_VALID);
		}
		status = TicketStatus.USED;
		checkedInAt = now;
	}

}

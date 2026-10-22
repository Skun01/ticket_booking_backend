package com.example.base.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "activity_logs", uniqueConstraints = @UniqueConstraint(
		name = "uk_activity_logs_message_action", columnNames = {"message_id", "action"}))
public class ActivityLog extends BaseEntity {

	@Column(nullable = false, length = 36)
	private String messageId;

	@Column(nullable = false)
	private Long userId;

	@Column(nullable = false, length = 50)
	private String action;

	@Column(nullable = false, length = 50)
	private String entityType;

	@Column(nullable = false)
	private Long entityId;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String detail;

	public ActivityLog(String messageId, Order order, Instant occurredAt) {
		this.messageId = messageId;
		this.userId = order.getUser().getId();
		this.action = "ORDER_" + order.getStatus().name();
		this.entityType = "ORDER";
		this.entityId = order.getId();
		this.detail = "Order %s changed to %s; totalAmount=%s; occurredAt=%s".formatted(
				order.getOrderCode(), order.getStatus(), order.getTotalAmount(), occurredAt);
	}

}

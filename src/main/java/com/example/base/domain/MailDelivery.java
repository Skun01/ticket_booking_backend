package com.example.base.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "mail_deliveries")
public class MailDelivery extends BaseEntity {

	@Column(nullable = false, unique = true)
	private Long orderId;

	@Column(nullable = false, length = 36)
	private String messageId;

	private Instant sentAt;

	public boolean isSent() {
		return sentAt != null;
	}

	public void markSent(Instant sentAt) {
		this.sentAt = sentAt;
	}

}

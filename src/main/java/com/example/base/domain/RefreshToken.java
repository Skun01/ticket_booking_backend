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
@Table(name = "refresh_tokens")
public class RefreshToken extends BaseEntity {

	@Column(nullable = false, unique = true, length = 64)
	private String tokenHash;

	@Column(nullable = false)
	private Long userId;

	@Column(nullable = false)
	private Instant expiresAt;

	private Instant revokedAt;

	public RefreshToken(String tokenHash, Long userId, Instant expiresAt) {
		this.tokenHash = tokenHash;
		this.userId = userId;
		this.expiresAt = expiresAt;
	}

	public boolean isExpired(Instant now) {
		return !expiresAt.isAfter(now);
	}

	public boolean isRevoked() {
		return revokedAt != null;
	}

	public void revoke(Instant now) {
		revokedAt = now;
	}

}

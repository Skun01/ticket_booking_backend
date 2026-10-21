package com.example.base.config.property;

import java.time.Duration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app.jwt")
public record JwtProperties(
		@NotBlank String secret,
		@NotBlank String issuer,
		@NotNull Duration accessTokenTtl,
		@NotNull Duration refreshTokenTtl,
		boolean refreshCookieSecure) {
}

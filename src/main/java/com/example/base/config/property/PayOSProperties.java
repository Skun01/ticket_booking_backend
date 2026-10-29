package com.example.base.config.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app.payos")
public record PayOSProperties(
		String clientId,
		String apiKey,
		String checksumKey,
		String returnUrl,
		String cancelUrl) {
}

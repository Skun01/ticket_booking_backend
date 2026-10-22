package com.example.base.config.property;

import jakarta.validation.constraints.Positive;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app.order")
public record OrderProperties(
		@DefaultValue("15") @Positive int holdMinutes) {
}

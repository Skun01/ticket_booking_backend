package com.example.base.config.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("app.order")
public record OrderProperties(
		@DefaultValue("15") int holdMinutes) {
}

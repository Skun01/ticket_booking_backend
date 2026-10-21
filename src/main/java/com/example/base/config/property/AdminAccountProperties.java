package com.example.base.config.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("app.admin")
public record AdminAccountProperties(
		String email,
		String password,
		@DefaultValue("Administrator") String fullName) {
}

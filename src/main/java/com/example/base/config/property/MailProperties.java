package com.example.base.config.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.mail-sender")
public record MailProperties(
		String from) {
}

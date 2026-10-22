package com.example.base.config.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.rabbitmq")
public record RabbitProperties(
		String exchange,
		String mailQueue,
		String logQueue,
		String routingKeyPaid,
		String routingKeyCancelled) {
}

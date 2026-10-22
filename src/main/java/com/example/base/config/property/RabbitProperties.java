package com.example.base.config.property;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app.rabbitmq")
public record RabbitProperties(
		@NotBlank String exchange,
		@NotBlank String mailQueue,
		@NotBlank String logQueue,
		@NotBlank String routingKeyPaid,
		@NotBlank String routingKeyCancelled,
		@DefaultValue("order.dlx") @NotBlank String deadLetterExchange,
		@DefaultValue("mail.dlq") @NotBlank String mailDeadLetterQueue,
		@DefaultValue("log.dlq") @NotBlank String logDeadLetterQueue,
		@DefaultValue("2") @Min(0) @Max(10) int consumerMaxRetries,
		@DefaultValue("1000") @Min(100) @Max(60000) long consumerRetryDelayMillis,
		@DefaultValue("10") @Min(1) @Max(1000) int consumerPrefetch,
		@DefaultValue("true") boolean declareDeadLetterArguments) {
}

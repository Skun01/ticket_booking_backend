package com.example.base.config.property;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app.rabbitmq")
public record RabbitProperties(
		@NotNull @Valid @DefaultValue Listener listener,
		@NotNull @Valid @DefaultValue OrderEvent orderEvent,
		@NotNull @Valid @DefaultValue OrderExpiry orderExpiry) {

	public record Listener(
			@DefaultValue("10") @Min(1) @Max(1000) int prefetch,
			@DefaultValue("2") @Min(0) @Max(10) int maxRetries,
			@DefaultValue("1000") @Min(100) @Max(60000) long retryDelayMillis,
			@DefaultValue("true") boolean declareDeadLetterArguments) {
	}

	public record OrderEvent(
			@DefaultValue("order.exchange") @NotBlank String exchange,
			@DefaultValue("order.dlx") @NotBlank String deadLetterExchange,
			@DefaultValue("order.paid") @NotBlank String routingKeyPaid,
			@DefaultValue("order.cancelled") @NotBlank String routingKeyCancelled,
			@DefaultValue("mail.queue") @NotBlank String mailQueue,
			@DefaultValue("mail.dlq") @NotBlank String mailDeadLetterQueue,
			@DefaultValue("log.queue") @NotBlank String logQueue,
			@DefaultValue("log.dlq") @NotBlank String logDeadLetterQueue) {
	}

	public record OrderExpiry(
			@DefaultValue("order.hold.exchange") @NotBlank String holdExchange,
			@DefaultValue("order.hold.queue") @NotBlank String holdQueue,
			@DefaultValue("order.expire.queue") @NotBlank String expireQueue,
			@DefaultValue("order.expire.dlq") @NotBlank String expireDeadLetterQueue,
			@DefaultValue("order.hold") @NotBlank String routingKeyHold,
			@DefaultValue("order.expired") @NotBlank String routingKeyExpired) {
	}

}

package com.example.base.config.property;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app.mail-sender")
public record MailProperties(
		@NotBlank @Email String from) {
}

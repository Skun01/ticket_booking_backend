package com.example.base.config.property;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app.cache")
public record CacheProperties(
		@DefaultValue("10m") Duration defaultTtl,
		@DefaultValue("30m") Duration eventDetailTtl) {
}


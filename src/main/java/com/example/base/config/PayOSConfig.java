package com.example.base.config;

import com.example.base.config.property.PayOSProperties;
import vn.payos.PayOS;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PayOSConfig {

	@Bean
	public PayOS payOS(PayOSProperties properties) {
		return new PayOS(properties.clientId(), properties.apiKey(), properties.checksumKey());
	}

}

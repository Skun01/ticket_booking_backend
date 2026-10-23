package com.example.base.config.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("app.organizer")
public record OrganizerAccountProperties(
		String email,
		String password,
		@DefaultValue("Organizer") String fullName) {

}


package com.example.base.config;

import com.example.base.config.property.OrganizerAccountProperties;
import com.example.base.domain.enums.Role;
import com.example.base.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

// khởi tạo organizer account
@Slf4j
@Component
@RequiredArgsConstructor
public class OrganizerAccountInitializer implements ApplicationRunner {

	private final OrganizerAccountProperties properties;
	private final UserService userService;

	@Override
	public void run(ApplicationArguments args) {
		if (!StringUtils.hasText(properties.email()) || !StringUtils.hasText(properties.password())) {
			return;
		}
		if (userService.findByEmail(properties.email()).isPresent()) {
			return;
		}
		userService.create(properties.email(), properties.password(), properties.fullName(), null, Role.ORGANIZER);
		log.info("Created organizer account {}", properties.email());
	}

}


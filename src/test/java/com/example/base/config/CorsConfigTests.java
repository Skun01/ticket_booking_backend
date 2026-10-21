package com.example.base.config;

import com.example.base.support.IntegrationTest;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// application-test.yaml cho phép origin http://localhost:[*]
@IntegrationTest
class CorsConfigTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void preflightFromAllowedOriginIsAccepted() throws Exception {
		mockMvc.perform(options("/api/auth/login")
						.header(HttpHeaders.ORIGIN, "http://localhost:5173")
						.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"))
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
	}

	@Test
	void preflightFromUnknownOriginIsRejected() throws Exception {
		mockMvc.perform(options("/api/auth/login")
						.header(HttpHeaders.ORIGIN, "https://evil.example")
						.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
				.andExpect(status().isForbidden())
				.andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
	}

}

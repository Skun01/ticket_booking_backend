package com.example.base.config;

import com.example.base.support.IntegrationTest;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static com.example.base.support.AuthTestUtils.accessToken;
import static com.example.base.support.AuthTestUtils.login;
import static com.example.base.support.AuthTestUtils.register;
import static com.example.base.support.AuthTestUtils.uniqueEmail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class AuthorizationTests {

	private static final String ADMIN_ONLY = "/api/test/admin-only";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void userIsForbiddenFromAdminEndpoint() throws Exception {
		String token = accessToken(register(mockMvc, uniqueEmail()).andReturn());

		mockMvc.perform(get(ADMIN_ONLY).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("FORBIDDEN"));
	}

	// Tài khoản admin được AdminAccountInitializer tạo từ cấu hình app.admin trong application-test.yaml
	@Test
	void seededAdminCanAccessAdminEndpoint() throws Exception {
		String token = accessToken(login(mockMvc, "admin@test.local", "admin12345")
				.andExpect(status().isOk())
				.andReturn());

		mockMvc.perform(get(ADMIN_ONLY).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk());
	}

	@Test
	void swaggerEndpointsArePubliclyAccessible() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk());

		mockMvc.perform(get("/swagger-ui/index.html"))
				.andExpect(status().isOk());
	}

}

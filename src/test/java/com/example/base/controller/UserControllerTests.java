package com.example.base.controller;

import com.example.base.support.IntegrationTest;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static com.example.base.support.AuthTestUtils.accessToken;
import static com.example.base.support.AuthTestUtils.register;
import static com.example.base.support.AuthTestUtils.uniqueEmail;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class UserControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void meReturnsCurrentUserWithoutPassword() throws Exception {
		String email = uniqueEmail();
		String token = accessToken(register(mockMvc, email).andReturn());

		mockMvc.perform(get("/api/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value(email))
				.andExpect(jsonPath("$.role").value("USER"))
				.andExpect(jsonPath("$.password").doesNotExist());
	}

	@Test
	void meWithoutTokenReturnsProblemDetail() throws Exception {
		mockMvc.perform(get("/api/users/me"))
				.andExpect(status().isUnauthorized())
				.andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, startsWith("Bearer")))
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
	}

	@Test
	void meWithInvalidTokenReturnsProblemDetail() throws Exception {
		mockMvc.perform(get("/api/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
				.andExpect(status().isUnauthorized())
				.andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, containsString("invalid_token")))
				.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
	}

}

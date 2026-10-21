package com.example.base.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.example.base.support.IntegrationTest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static com.example.base.support.AuthTestUtils.PASSWORD;
import static com.example.base.support.AuthTestUtils.REFRESH_COOKIE;
import static com.example.base.support.AuthTestUtils.login;
import static com.example.base.support.AuthTestUtils.refreshCookie;
import static com.example.base.support.AuthTestUtils.register;
import static com.example.base.support.AuthTestUtils.uniqueEmail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class AuthControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void registerReturnsAccessTokenAndHttpOnlyRefreshCookie() throws Exception {
		register(mockMvc, uniqueEmail())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(cookie().httpOnly(REFRESH_COOKIE, true))
				.andExpect(cookie().path(REFRESH_COOKIE, "/api/auth"));
	}

	@Test
	void registerWithExistingEmailReturnsConflict() throws Exception {
		String email = uniqueEmail();
		register(mockMvc, email).andExpect(status().isCreated());

		register(mockMvc, email)
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("EMAIL_ALREADY_REGISTERED"));
	}

	@Test
	void concurrentRegistrationsWithSameEmailCreateOnlyOneUser() throws Exception {
		String email = uniqueEmail();
		int attempts = 5;
		CyclicBarrier barrier = new CyclicBarrier(attempts);
		Callable<Integer> attempt = () -> {
			barrier.await();
			return register(mockMvc, email).andReturn().getResponse().getStatus();
		};
		ExecutorService executor = Executors.newFixedThreadPool(attempts);
		List<Integer> statuses = new ArrayList<>();
		try {
			for (Future<Integer> result : executor.invokeAll(Collections.nCopies(attempts, attempt))) {
				statuses.add(result.get());
			}
		}
		finally {
			executor.shutdownNow();
		}

		assertThat(Collections.frequency(statuses, 201)).isEqualTo(1);
		assertThat(Collections.frequency(statuses, 409)).isEqualTo(attempts - 1);
	}

	@Test
	void registerWithInvalidBodyReturnsFieldErrors() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email": "not-an-email", "password": "short", "fullName": ""}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
				.andExpect(jsonPath("$.errors[*].field", containsInAnyOrder("email", "password", "fullName")));
	}

	@Test
	void malformedJsonReturnsProblemDetailWithStatusCode() throws Exception {
		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("BAD_REQUEST"));
	}

	@Test
	void loginReturnsAccessToken() throws Exception {
		String email = uniqueEmail();
		register(mockMvc, email).andExpect(status().isCreated());

		login(mockMvc, email, PASSWORD)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty());
	}

	@Test
	void loginWithWrongPasswordReturnsUnauthorized() throws Exception {
		String email = uniqueEmail();
		register(mockMvc, email).andExpect(status().isCreated());

		login(mockMvc, email, "wrong-password")
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
	}

	@Test
	void refreshRotatesRefreshToken() throws Exception {
		Cookie oldCookie = refreshCookie(register(mockMvc, uniqueEmail()).andReturn());

		MvcResult refreshed = mockMvc.perform(post("/api/auth/refresh").cookie(oldCookie))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andReturn();

		assertThat(refreshCookie(refreshed).getValue()).isNotEqualTo(oldCookie.getValue());
	}

	@Test
	void refreshIgnoresAuthorizationHeader() throws Exception {
		Cookie cookie = refreshCookie(register(mockMvc, uniqueEmail()).andReturn());

		mockMvc.perform(post("/api/auth/refresh")
						.cookie(cookie)
						.header(HttpHeaders.AUTHORIZATION, "Bearer expired-or-invalid"))
				.andExpect(status().isOk());
	}

	@Test
	void reusingRotatedRefreshTokenRevokesAllSessions() throws Exception {
		Cookie oldCookie = refreshCookie(register(mockMvc, uniqueEmail()).andReturn());
		Cookie newCookie = refreshCookie(mockMvc.perform(post("/api/auth/refresh").cookie(oldCookie))
				.andExpect(status().isOk())
				.andReturn());

		mockMvc.perform(post("/api/auth/refresh").cookie(oldCookie))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"));
		// Token mới cấp cũng phải bị thu hồi theo, dù request trước kết thúc bằng lỗi 401
		mockMvc.perform(post("/api/auth/refresh").cookie(newCookie))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void refreshWithoutCookieReturnsUnauthorized() throws Exception {
		mockMvc.perform(post("/api/auth/refresh"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"));
	}

	@Test
	void logoutRevokesRefreshTokenAndClearsCookie() throws Exception {
		Cookie cookie = refreshCookie(register(mockMvc, uniqueEmail()).andReturn());

		mockMvc.perform(post("/api/auth/logout").cookie(cookie))
				.andExpect(status().isNoContent())
				.andExpect(cookie().maxAge(REFRESH_COOKIE, 0));

		mockMvc.perform(post("/api/auth/refresh").cookie(cookie))
				.andExpect(status().isUnauthorized());
	}

}

package com.example.base.support;

import java.util.UUID;

import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public final class AuthTestUtils {

	public static final String PASSWORD = "password123";
	public static final String REFRESH_COOKIE = "refresh_token";

	private AuthTestUtils() {
	}

	// Email ngẫu nhiên để các test không đụng nhau khi dùng chung database
	public static String uniqueEmail() {
		return "user-" + UUID.randomUUID() + "@example.com";
	}

	public static ResultActions register(MockMvc mockMvc, String email) throws Exception {
		return mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email": "%s", "password": "%s", "fullName": "Test User"}
						""".formatted(email, PASSWORD)));
	}

	public static ResultActions login(MockMvc mockMvc, String email, String password) throws Exception {
		return mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email": "%s", "password": "%s"}
						""".formatted(email, password)));
	}

	public static String accessToken(MvcResult result) throws Exception {
		return JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
	}

	public static Cookie refreshCookie(MvcResult result) {
		return result.getResponse().getCookie(REFRESH_COOKIE);
	}

}

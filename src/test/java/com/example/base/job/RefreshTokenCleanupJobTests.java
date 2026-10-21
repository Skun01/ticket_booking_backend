package com.example.base.job;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import com.example.base.domain.RefreshToken;
import com.example.base.repository.RefreshTokenRepository;
import com.example.base.service.TokenService;
import com.example.base.service.UserService;
import com.example.base.support.IntegrationTest;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static com.example.base.support.AuthTestUtils.register;
import static com.example.base.support.AuthTestUtils.uniqueEmail;
import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class RefreshTokenCleanupJobTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserService userService;

	@Autowired
	private TokenService tokenService;

	@Autowired
	private RefreshTokenRepository refreshTokenRepository;

	@Autowired
	private RefreshTokenCleanupJob cleanupJob;

	@Test
	void deletesExpiredTokensButKeepsRevokedOnesUntilTheyExpire() throws Exception {
		String email = uniqueEmail();
		register(mockMvc, email);
		Long userId = userService.findByEmail(email).orElseThrow().getId();
		Instant now = Instant.now();

		RefreshToken expired = refreshTokenRepository.save(
				new RefreshToken(randomHash(), userId, now.minus(Duration.ofDays(1))));
		RefreshToken revoked = new RefreshToken(randomHash(), userId, now.plus(Duration.ofDays(1)));
		revoked.revoke(now);
		refreshTokenRepository.save(revoked);

		cleanupJob.deleteExpiredTokens();

		assertThat(refreshTokenRepository.findById(expired.getId())).isEmpty();
		assertThat(refreshTokenRepository.findById(revoked.getId())).isPresent();
	}

	private String randomHash() {
		return tokenService.hash(UUID.randomUUID().toString());
	}

}

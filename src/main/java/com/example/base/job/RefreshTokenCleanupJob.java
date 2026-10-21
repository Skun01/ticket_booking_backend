package com.example.base.job;

import java.time.Instant;

import com.example.base.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupJob {

	private final RefreshTokenRepository refreshTokenRepository;

	// chỉ xóa token đã hết hạn
	@Scheduled(cron = "${app.jwt.refresh-token-cleanup-cron}")
	@Transactional
	public void deleteExpiredTokens() {
		int deleted = refreshTokenRepository.deleteAllExpiredBefore(Instant.now());
		log.info("Deleted {} expired refresh tokens", deleted);
	}

}

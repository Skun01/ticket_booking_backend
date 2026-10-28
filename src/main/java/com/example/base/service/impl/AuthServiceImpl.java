package com.example.base.service.impl;

import java.time.Instant;
import java.util.UUID;

import com.example.base.config.property.JwtProperties;
import com.example.base.domain.RefreshToken;
import com.example.base.domain.User;
import com.example.base.domain.enums.Role;
import com.example.base.dto.auth.AuthTokens;
import com.example.base.dto.auth.LoginRequest;
import com.example.base.dto.auth.RegisterRequest;
import com.example.base.exception.AppException;
import com.example.base.exception.AuthErrorCode;
import com.example.base.repository.RefreshTokenRepository;
import com.example.base.service.AuthService;
import com.example.base.service.TokenService;
import com.example.base.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private final UserService userService;
	private final TokenService tokenService;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtProperties jwtProperties;

	@Override
	@Transactional
	public AuthTokens register(RegisterRequest request) {
		User user = userService.create(request.email(), request.password(),
				request.fullName(), request.phone(), Role.CUSTOMER);

		return issueTokens(user, UUID.randomUUID().toString());
	}

	@Override
	@Transactional
	public AuthTokens login(LoginRequest request) {
		User user = userService.findByEmail(request.email())
				.filter(found -> passwordEncoder.matches(request.password(), found.getPassword()))
				.orElseThrow(() -> new AppException(AuthErrorCode.INVALID_CREDENTIALS));

		return issueTokens(user, UUID.randomUUID().toString());
	}

	@Override
	@Transactional(noRollbackFor = AppException.class)
	public AuthTokens refresh(String rawRefreshToken) {
		if (rawRefreshToken == null) {
			throw invalidRefreshToken();
		}
		Instant now = Instant.now();
		RefreshToken token = refreshTokenRepository.findByTokenHash(tokenService.hash(rawRefreshToken))
				.orElseThrow(AuthServiceImpl::invalidRefreshToken);

		if (token.isRevoked()) {
			refreshTokenRepository.revokeAllByFamilyId(token.getFamilyId(), now);

			throw invalidRefreshToken();
		}
		if (token.isExpired(now)) {
			throw invalidRefreshToken();
		}
		token.revoke(now);

		return issueTokens(userService.getById(token.getUserId()), token.getFamilyId());
	}

	@Override
	@Transactional
	public void logout(String rawRefreshToken) {
		refreshTokenRepository.findByTokenHash(tokenService.hash(rawRefreshToken))
				.ifPresent(token -> refreshTokenRepository.revokeAllByFamilyId(token.getFamilyId(), Instant.now()));
	}

	private AuthTokens issueTokens(User user, String familyId) {
		String refreshToken = tokenService.generateRefreshToken();
		Instant expiresAt = Instant.now().plus(jwtProperties.refreshTokenTtl());
		refreshTokenRepository.save(
				new RefreshToken(tokenService.hash(refreshToken), user.getId(), familyId, expiresAt));

		return new AuthTokens(tokenService.createAccessToken(user), refreshToken);
	}

	private static AppException invalidRefreshToken() {
		return new AppException(AuthErrorCode.INVALID_REFRESH_TOKEN);
	}

}

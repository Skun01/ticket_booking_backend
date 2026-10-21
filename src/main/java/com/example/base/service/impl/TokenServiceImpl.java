package com.example.base.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

import com.example.base.config.property.JwtProperties;
import com.example.base.domain.User;
import com.example.base.service.TokenService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

	private final JwtEncoder jwtEncoder;
	private final JwtProperties jwtProperties;
	private final SecureRandom secureRandom = new SecureRandom();

	@Override
	public String createAccessToken(User user) {
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(jwtProperties.issuer())
				.subject(user.getId().toString())
				.issuedAt(now)
				.expiresAt(now.plus(jwtProperties.accessTokenTtl()))
				.claim("roles", List.of(user.getRole().name()))
				.build();
		// Phải khai báo HS256 vì header mặc định của JwtEncoder là RS256
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256)
				.build();

		return jwtEncoder.encode(JwtEncoderParameters.from(header, claims))
				.getTokenValue();
	}

	@Override
	public String generateRefreshToken() {
		byte[] bytes = new byte[32];
		secureRandom.nextBytes(bytes);

		return Base64.getUrlEncoder()
				.withoutPadding()
				.encodeToString(bytes);
	}

	@Override
	public String hash(String token) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");

			return HexFormat.of()
					.formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
		}
		catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException(ex);
		}
	}

}

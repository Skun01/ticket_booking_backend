package com.example.base.repository;

import java.time.Instant;
import java.util.Optional;

import com.example.base.domain.RefreshToken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	Optional<RefreshToken> findByTokenHash(String tokenHash);

	@Modifying
	@Query("""
			update RefreshToken t
			set t.revokedAt = :now, t.updatedAt = :now
			where t.userId = :userId and t.revokedAt is null
			""")
	void revokeAllByUserId(@Param("userId") Long userId, @Param("now") Instant now);

	@Modifying
	@Query("delete from RefreshToken t where t.expiresAt < :now")
	int deleteAllExpiredBefore(@Param("now") Instant now);

}

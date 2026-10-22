package com.example.base.repository;

import java.util.Optional;

import com.example.base.domain.MailDelivery;
import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MailDeliveryRepository extends JpaRepository<MailDelivery, Long> {

	// Upsert lấy khóa duy nhất theo order ngay cả khi hai consumer cùng xử lý lần giao đầu tiên.
	@Modifying
	@Query(value = """
			INSERT INTO mail_deliveries (order_id, message_id, created_at, updated_at)
			VALUES (:orderId, :messageId, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6))
			ON DUPLICATE KEY UPDATE id = id
			""", nativeQuery = true)
	int prepare(@Param("orderId") Long orderId, @Param("messageId") String messageId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<MailDelivery> findLockedByOrderId(Long orderId);

}

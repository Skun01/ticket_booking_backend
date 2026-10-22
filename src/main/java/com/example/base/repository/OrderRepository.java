package com.example.base.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.example.base.domain.Order;
import com.example.base.domain.enums.OrderStatus;
import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface OrderRepository extends JpaRepository<Order, Long> {

	Page<Order> findByUserId(Long userId, Pageable pageable);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<Order> findLockedById(Long id);

	List<Order> findByStatusAndExpiresAtLessThanEqualOrderByExpiresAtAscIdAsc(
			OrderStatus status, Instant cutoff, Pageable pageable);

}

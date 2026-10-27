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
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

	Page<Order> findByUserId(Long userId, Pageable pageable);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<Order> findLockedById(Long id);

	@Query("""
			SELECT o.id FROM Order o
			WHERE o.status = :status AND o.expiresAt <= :cutoff
			ORDER BY o.expiresAt ASC, o.id ASC
			""")
	List<Long> findExpiredIds(@Param("status") OrderStatus status, @Param("cutoff") Instant cutoff, Pageable pageable);

}

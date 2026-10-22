package com.example.base.repository;

import java.util.List;
import java.util.Optional;

import com.example.base.domain.TicketType;
import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {

	List<TicketType> findByEventIdOrderByIdAsc(Long eventId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<TicketType> findLockedById(Long id);

	boolean existsByEventId(Long eventId);

	@Query(value = "SELECT event_id FROM ticket_types WHERE id = :id", nativeQuery = true)
	Optional<Long> findEventIdById(@Param("id") Long id);

	// UPDATE có điều kiện kiểm tra tồn kho ngay trên hàng đang khóa, không dựa vào entity đã đọc trước đó.
	@Modifying(flushAutomatically = true)
	@Query(value = """
			UPDATE ticket_types
			SET sold_quantity = sold_quantity + :quantity, updated_at = UTC_TIMESTAMP(6)
			WHERE id = :id AND :quantity > 0 AND :quantity <= total_quantity - sold_quantity
			""", nativeQuery = true)
	int reserve(@Param("id") Long id, @Param("quantity") int quantity);

	@Modifying(flushAutomatically = true)
	@Query(value = """
			UPDATE ticket_types
			SET sold_quantity = sold_quantity - :quantity, updated_at = UTC_TIMESTAMP(6)
			WHERE id = :id AND :quantity > 0 AND sold_quantity >= :quantity
			""", nativeQuery = true)
	int releaseSold(@Param("id") Long id, @Param("quantity") int quantity);

}

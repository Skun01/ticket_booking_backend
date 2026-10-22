package com.example.base.repository;

import java.util.List;
import java.util.Optional;

import com.example.base.domain.TicketType;
import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {

	List<TicketType> findByEventIdOrderByIdAsc(Long eventId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<TicketType> findLockedById(Long id);

	boolean existsByEventId(Long eventId);

}

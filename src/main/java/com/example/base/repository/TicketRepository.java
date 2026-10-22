package com.example.base.repository;

import java.util.List;
import java.util.Optional;

import com.example.base.domain.Ticket;
import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

	@EntityGraph(attributePaths = {"orderItem.order", "orderItem.ticketType.event"})
	Page<Ticket> findByOrderItemOrderUserId(Long userId, Pageable pageable);

	@EntityGraph(attributePaths = {"orderItem.ticketType.event.venue"})
	List<Ticket> findByOrderItemOrderIdOrderByIdAsc(Long orderId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<Ticket> findLockedByTicketCode(String ticketCode);

}

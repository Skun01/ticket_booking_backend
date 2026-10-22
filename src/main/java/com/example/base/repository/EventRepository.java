package com.example.base.repository;

import java.util.Optional;

import com.example.base.domain.Event;
import com.example.base.domain.enums.EventStatus;
import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

	// Không fetch collection ở truy vấn phân trang để tránh nhân bản dòng và phân trang trong bộ nhớ.
	@Override
	@EntityGraph(attributePaths = {"category", "venue"})
	Page<Event> findAll(Specification<Event> specification, Pageable pageable);

	@EntityGraph(attributePaths = {"category", "venue"})
	Optional<Event> findByIdAndStatus(Long id, EventStatus status);

	@EntityGraph(attributePaths = {"category", "venue"})
	Page<Event> findByOrganizerId(Long organizerId, Pageable pageable);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<Event> findLockedById(Long id);

	@Lock(LockModeType.PESSIMISTIC_READ)
	Optional<Event> findBookingById(Long id);

	boolean existsByCategoryId(Long categoryId);

	boolean existsByVenueId(Long venueId);

}

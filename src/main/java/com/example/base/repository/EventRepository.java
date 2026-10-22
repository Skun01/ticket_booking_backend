package com.example.base.repository;

import java.util.Optional;

import com.example.base.domain.Event;
import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

	@EntityGraph(attributePaths = {"category", "venue"})
	Page<Event> findByOrganizerId(Long organizerId, Pageable pageable);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<Event> findLockedById(Long id);

	boolean existsByCategoryId(Long categoryId);

	boolean existsByVenueId(Long venueId);

}

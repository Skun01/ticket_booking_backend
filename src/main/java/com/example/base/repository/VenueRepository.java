package com.example.base.repository;

import java.util.Optional;

import com.example.base.domain.Venue;
import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface VenueRepository extends JpaRepository<Venue, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<Venue> findLockedById(Long id);

}

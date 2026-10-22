package com.example.base.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.example.base.domain.enums.EventStatus;
import com.example.base.exception.AppException;
import com.example.base.exception.EventErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "events")
public class Event extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "organizer_id", nullable = false)
	private User organizer;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "venue_id", nullable = false)
	private Venue venue;

	@Column(nullable = false, length = 200)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(length = 500)
	private String bannerUrl;

	@Column(nullable = false)
	private Instant startTime;

	@Column(nullable = false)
	private Instant endTime;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EventStatus status;

	@OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
	@OrderBy("id ASC")
	@BatchSize(size = 50)
	private List<TicketType> ticketTypes = new ArrayList<>();

	public Event(User organizer, Category category, Venue venue, String title, String description,
			String bannerUrl, Instant startTime, Instant endTime) {
		this.organizer = organizer;
		this.status = EventStatus.DRAFT;
		update(category, venue, title, description, bannerUrl, startTime, endTime);
	}

	public void update(Category category, Venue venue, String title, String description,
			String bannerUrl, Instant startTime, Instant endTime) {
		requireDraft();
		if (!endTime.isAfter(startTime)) {
			throw new AppException(EventErrorCode.INVALID_EVENT_TIME);
		}
		this.category = category;
		this.venue = venue;
		this.title = title;
		this.description = description;
		this.bannerUrl = bannerUrl;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public void publish() {
		requireDraft();
		if (!startTime.isAfter(Instant.now())) {
			throw new AppException(EventErrorCode.EVENT_START_IN_PAST);
		}
		this.status = EventStatus.PUBLISHED;
	}

	public void cancel() {
		if (status != EventStatus.DRAFT && status != EventStatus.PUBLISHED) {
			throw new AppException(EventErrorCode.INVALID_STATUS_TRANSITION);
		}
		this.status = EventStatus.CANCELLED;
	}

	public void end() {
		if (status != EventStatus.PUBLISHED || endTime.isAfter(Instant.now())) {
			throw new AppException(EventErrorCode.INVALID_STATUS_TRANSITION);
		}
		this.status = EventStatus.ENDED;
	}

	public void requireDraft() {
		if (status != EventStatus.DRAFT) {
			throw new AppException(EventErrorCode.EVENT_NOT_EDITABLE);
		}
	}

	public boolean isOwnedBy(Long userId) {
		return organizer.getId().equals(userId);
	}

}

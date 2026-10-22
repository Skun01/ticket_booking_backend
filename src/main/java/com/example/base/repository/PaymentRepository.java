package com.example.base.repository;

import java.time.Instant;
import java.util.List;

import com.example.base.domain.Payment;
import com.example.base.dto.report.DailyRevenueProjection;
import com.example.base.dto.report.TopEventProjection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

	@Query(value = """
			SELECT DATE(p.paid_at) AS revenueDate,
			       COUNT(DISTINCT o.id) AS paidOrders, SUM(p.amount) AS revenue
			FROM payments p
			JOIN orders o ON o.id = p.order_id
			JOIN order_items oi ON oi.order_id = o.id
			JOIN ticket_types tt ON tt.id = oi.ticket_type_id
			WHERE p.status = 'SUCCESS' AND o.status = 'PAID' AND tt.event_id = :eventId
			GROUP BY DATE(p.paid_at)
			ORDER BY revenueDate ASC
			""", nativeQuery = true)
	List<DailyRevenueProjection> findDailyRevenueByEventId(@Param("eventId") Long eventId);

	// Mỗi đơn hiện chỉ có một hạng vé nên join order_items không nhân doanh thu thanh toán.
	@Query(value = """
			SELECT e.id AS eventId, e.title AS eventTitle,
			       SUM(oi.quantity) AS ticketsSold, SUM(p.amount) AS revenue
			FROM payments p
			JOIN orders o ON o.id = p.order_id
			JOIN order_items oi ON oi.order_id = o.id
			JOIN ticket_types tt ON tt.id = oi.ticket_type_id
			JOIN events e ON e.id = tt.event_id
			WHERE p.status = 'SUCCESS' AND o.status = 'PAID'
			  AND p.paid_at >= :from AND p.paid_at < :to
			GROUP BY e.id, e.title
			ORDER BY ticketsSold DESC, revenue DESC, eventId ASC
			LIMIT 10
			""", nativeQuery = true)
	List<TopEventProjection> findTopEvents(@Param("from") Instant from, @Param("to") Instant to);

}

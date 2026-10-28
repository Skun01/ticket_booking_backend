package com.example.base.repository.impl;

import java.time.Instant;
import java.util.List;

import com.example.base.dto.report.DailyRevenueResponse;
import com.example.base.repository.PaymentRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryCustomImpl implements PaymentRepositoryCustom {

	private final EntityManager entityManager;

	@Override
	public List<DailyRevenueResponse> findDailyRevenueByProcedure(Instant from, Instant to) {
		StoredProcedureQuery procedure = entityManager.createStoredProcedureQuery("sp_revenue_report");
		procedure.registerStoredProcedureParameter(1, Instant.class, ParameterMode.IN);
		procedure.registerStoredProcedureParameter(2, Instant.class, ParameterMode.IN);
		procedure.setParameter(1, from);
		procedure.setParameter(2, to);

		List<?> rows = procedure.getResultList();

		return rows.stream()
				.map(row -> (Object[]) row)
				.map(DailyRevenueResponse::from)
				.toList();
	}

}

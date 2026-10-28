package com.example.base.repository;

import java.time.Instant;
import java.util.List;

import com.example.base.dto.report.DailyRevenueResponse;

public interface PaymentRepositoryCustom {

	List<DailyRevenueResponse> findDailyRevenueByProcedure(Instant from, Instant to);

}

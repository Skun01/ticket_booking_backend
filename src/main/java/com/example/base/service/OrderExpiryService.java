package com.example.base.service;

import java.time.Instant;
import java.util.List;

public interface OrderExpiryService {

	List<Long> findExpiredIds(Instant cutoff, int limit);

	boolean expire(Long orderId);

}

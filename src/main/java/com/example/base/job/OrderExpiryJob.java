package com.example.base.job;

import java.time.Instant;

import com.example.base.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderExpiryJob {

	private final OrderService orderService;

	@Scheduled(fixedRate = 60_000)
	public void expireOrders() {
		int expired = 0;
		for (Long orderId : orderService.findExpiredIds(Instant.now(), 100)) {
			try {
				// Gọi qua bean khác để mỗi đơn có transaction riêng, không mắc lỗi self-invocation.
				if (orderService.expire(orderId)) {
					expired++;
				}
			}
			catch (RuntimeException ex) {
				log.error("Could not expire order {}", orderId, ex);
			}
		}
		if (expired > 0) {
			log.info("Expired {} pending orders", expired);
		}
	}

}

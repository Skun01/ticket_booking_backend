package com.example.base.service.impl;

import java.time.Instant;
import java.util.List;

import com.example.base.domain.Order;
import com.example.base.domain.enums.OrderStatus;
import com.example.base.repository.OrderRepository;
import com.example.base.service.OrderExpiryService;
import com.example.base.service.OrderStockService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderExpiryServiceImpl implements OrderExpiryService {

	private final OrderRepository orderRepository;
	private final OrderStockService orderStockService;

	@Override
	public List<Long> findExpiredIds(Instant cutoff, int limit) {
		return orderRepository.findByStatusAndExpiresAtLessThanEqualOrderByExpiresAtAscIdAsc(
				OrderStatus.PENDING, cutoff, PageRequest.of(0, limit)).stream().map(Order::getId).toList();
	}

	@Override
	@Transactional
	public boolean expire(Long orderId) {
		Order order = orderRepository.findLockedById(orderId).orElse(null);
		if (order == null || !order.expire(Instant.now())) {
			return false;
		}
		orderStockService.release(order);

		return true;
	}

}

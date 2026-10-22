package com.example.base.service.impl;

import com.example.base.domain.ActivityLog;
import com.example.base.domain.Order;
import com.example.base.dto.order.OrderMessage;
import com.example.base.exception.InvalidOrderMessageException;
import com.example.base.notification.OrderMessageValidator;
import com.example.base.repository.ActivityLogRepository;
import com.example.base.repository.OrderRepository;
import com.example.base.service.ActivityLogService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

	private final ActivityLogRepository activityLogRepository;
	private final OrderRepository orderRepository;
	private final OrderMessageValidator messageValidator;

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
	public void record(OrderMessage message) {
		// Khóa theo order để kiểm tra trùng và ghi log không bị race giữa các consumer/instance.
		Order order = orderRepository.findLockedById(message.orderId())
				.orElseThrow(InvalidOrderMessageException::new);
		messageValidator.requireMatches(message, order);
		ActivityLog activityLog = new ActivityLog(message.messageId(), order, message.occurredAt());
		if (activityLogRepository.existsByMessageIdAndAction(message.messageId(), activityLog.getAction())) {
			return;
		}
		activityLogRepository.saveAndFlush(activityLog);
	}

}

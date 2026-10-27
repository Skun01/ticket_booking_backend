package com.example.base.notification;

import java.time.Instant;

import com.example.base.domain.MailDelivery;
import com.example.base.dto.notification.PaidOrderMailData;
import com.example.base.dto.order.OrderMessage;
import com.example.base.exception.AppException;
import com.example.base.exception.NotificationErrorCode;
import com.example.base.repository.MailDeliveryRepository;
import com.example.base.service.OrderMailService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OrderMailProcessor {

	private final MailDeliveryRepository mailDeliveryRepository;
	private final OrderMailService orderMailService;
	private final OrderMailSender mailSender;

	@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
	public void process(OrderMessage message) {
		PaidOrderMailData data = orderMailService.loadPaidOrder(message);

		mailDeliveryRepository.prepare(message.orderId(), message.messageId());
		MailDelivery delivery = mailDeliveryRepository.findLockedByOrderId(message.orderId())
				.orElseThrow(() -> new AppException(NotificationErrorCode.MAIL_DELIVERY_NOT_PREPARED));
		if (delivery.isSent()) {
			return;
		}
		mailSender.send(data);

		delivery.markSent(Instant.now());
	}

}

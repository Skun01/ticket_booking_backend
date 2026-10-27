package com.example.base.notification;

import com.example.base.config.property.RabbitProperties;
import com.example.base.domain.Order;
import com.example.base.domain.enums.OrderStatus;
import com.example.base.dto.order.OrderMessage;
import com.example.base.exception.InvalidOrderMessageException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;

import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderMessageValidator {

	private final Validator validator;
	private final RabbitProperties properties;

	public void validate(OrderMessage message, Message rawMessage, boolean paidOnly) {
		if (message == null || !validator.validate(message).isEmpty()) {
			throw new InvalidOrderMessageException();
		}
		String expectedRoutingKey = switch (message.status()) {
			case PAID -> properties.orderEvent().routingKeyPaid();
			case CANCELLED -> paidOnly ? null : properties.orderEvent().routingKeyCancelled();
			default -> null;
		};
		String headerId = rawMessage.getMessageProperties().getMessageId();
		if (expectedRoutingKey == null
				|| !expectedRoutingKey.equals(rawMessage.getMessageProperties().getReceivedRoutingKey())
				|| headerId != null && !headerId.equals(message.messageId())) {
			throw new InvalidOrderMessageException();
		}
	}

	public void requireMatches(OrderMessage message, Order order) {
		if (!order.getId().equals(message.orderId()) || !order.getOrderCode().equals(message.orderCode())
				|| !order.isOwnedBy(message.userId())
				|| order.getTotalAmount().compareTo(message.totalAmount()) != 0
				|| order.getStatus() != message.status()
				|| message.status() != OrderStatus.PAID && message.status() != OrderStatus.CANCELLED) {
			throw new InvalidOrderMessageException();
		}
	}

}

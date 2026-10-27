package com.example.base.notification;

import com.example.base.config.property.RabbitProperties;
import com.example.base.domain.enums.OrderStatus;
import com.example.base.dto.order.OrderMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

	private final RabbitTemplate rabbitTemplate;
	private final RabbitProperties properties;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void publish(OrderMessage message) {
		String routingKey = switch (message.status()) {
			case PAID -> properties.orderEvent().routingKeyPaid();
			case CANCELLED -> properties.orderEvent().routingKeyCancelled();
			case PENDING -> properties.orderExpiry().routingKeyHold();
			default -> null;
		};
		if (routingKey == null) {
			return;
		}
		try {
			String targetExchange = message.status() == OrderStatus.PENDING
					? properties.orderExpiry().holdExchange()
					: properties.orderEvent().exchange();

			rabbitTemplate.convertAndSend(targetExchange, routingKey, message, amqpMessage -> {
				amqpMessage.getMessageProperties().setMessageId(message.messageId());
				amqpMessage.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);

				return amqpMessage;
			});
		}
		catch (RuntimeException ex) {
			log.error("Order committed but RabbitMQ publish failed: messageId={}, orderId={}, routingKey={}",
					message.messageId(), message.orderId(), routingKey, ex);
		}
	}

}

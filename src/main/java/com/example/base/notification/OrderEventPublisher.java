package com.example.base.notification;

import com.example.base.config.property.RabbitProperties;
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
			case PAID -> properties.routingKeyPaid();
			case CANCELLED -> properties.routingKeyCancelled();
			default -> null;
		};
		if (routingKey == null) {
			return;
		}
		try {
			// Chỉ gửi sau commit để consumer không thấy đơn chưa commit hoặc đơn sẽ rollback.
			rabbitTemplate.convertAndSend(properties.exchange(), 
				routingKey, message, amqpMessage -> {
					amqpMessage.getMessageProperties().setMessageId(message.messageId());
					amqpMessage.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);

					return amqpMessage;
			});
		}
		catch (RuntimeException ex) {
			// DB đã commit nên không báo pay/cancel thất bại giả; cần outbox nếu muốn retry bền vững.
			log.error("Order committed but RabbitMQ publish failed: messageId={}, orderId={}, routingKey={}",
					message.messageId(), message.orderId(), routingKey, ex);
		}
	}

}

package com.example.base.notification;

import java.io.IOException;

import com.example.base.dto.order.OrderMessage;
import com.example.base.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderExpiryListener {

	private final OrderMessageHandler messageHandler;
	private final OrderService orderService;

	@RabbitListener(queues = "#{orderExpireQueue.name}", containerFactory = "orderListenerContainerFactory")
	public void receive(OrderMessage message, Message rawMessage, Channel channel) throws IOException {
		messageHandler.handle(rawMessage, channel, () -> {
			if (orderService.expire(message.orderId())) {
				log.info("Order {} expired and stock released via delayed message", message.orderId());
			}
		});
	}

}

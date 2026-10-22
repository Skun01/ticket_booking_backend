package com.example.base.notification;

import java.io.IOException;

import com.example.base.dto.order.OrderMessage;
import com.example.base.service.ActivityLogService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActivityLogListener {

	private final OrderMessageHandler messageHandler;
	private final OrderMessageValidator messageValidator;
	private final ActivityLogService activityLogService;

	@RabbitListener(queues = "#{logQueue.name}", containerFactory = "orderListenerContainerFactory")
	public void receive(OrderMessage message, Message rawMessage, Channel channel) throws IOException {
		messageHandler.handle(rawMessage, channel, () -> {
			messageValidator.validate(message, rawMessage, false);
			activityLogService.record(message);
		});
	}

}

package com.example.base.notification;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.core.Message;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderMessageHandler {

	private final RetryTemplate orderConsumerRetryTemplate;

	public void handle(Message message, Channel channel, Runnable operation) throws IOException {
		long tag = message.getMessageProperties().getDeliveryTag();
		try {
			orderConsumerRetryTemplate.invoke(operation);
		}
		catch (RuntimeException ex) {
			log.error("Order consumer rejected message: queue={}, messageId={}, failureType={}",
					message.getMessageProperties().getConsumerQueue(), message.getMessageProperties().getMessageId(),
					ex.getClass().getSimpleName());
			channel.basicReject(tag, false);

			return;
		}
		// ACK nằm ngoài retry nghiệp vụ để lỗi channel không làm gửi SMTP lại trong cùng lần xử lý.
		channel.basicAck(tag, false);
	}

}

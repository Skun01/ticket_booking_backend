package com.example.base.config;

import com.example.base.config.property.RabbitProperties;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

	private final RabbitProperties properties;

	public RabbitConfig(RabbitProperties properties) {
		this.properties = properties;
	}

	@Bean
	TopicExchange orderExchange() {
		return new TopicExchange(properties.exchange());
	}

	@Bean
	Queue mailQueue() {
		return QueueBuilder.durable(properties.mailQueue()).build();
	}

	@Bean
	Queue logQueue() {
		return QueueBuilder.durable(properties.logQueue()).build();
	}

	// mail.queue chỉ nhận message đơn đã thanh toán để gửi vé
	@Bean
	Binding mailBinding() {
		return BindingBuilder.bind(mailQueue()).to(orderExchange()).with(properties.routingKeyPaid());
	}

	// log.queue nhận mọi sự kiện order.* để ghi activity log
	@Bean
	Binding logBinding() {
		return BindingBuilder.bind(logQueue()).to(orderExchange()).with("order.#");
	}

	@Bean
	MessageConverter jsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setMessageConverter(messageConverter);

		return template;
	}

}

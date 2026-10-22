package com.example.base.config;

import java.time.Duration;

import com.example.base.config.property.RabbitProperties;
import com.example.base.exception.InvalidOrderMessageException;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.amqp.autoconfigure.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;

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
		return orderQueue(properties.mailQueue(), properties.mailDeadLetterQueue());
	}

	@Bean
	Queue logQueue() {
		return orderQueue(properties.logQueue(), properties.logDeadLetterQueue());
	}

	// mail.queue chỉ nhận message đơn đã thanh toán để gửi vé
	@Bean
	Binding mailBinding() {
		return BindingBuilder.bind(mailQueue())
			.to(orderExchange())
			.with(properties.routingKeyPaid());
	}

	// log.queue nhận mọi sự kiện order.* để ghi activity log
	@Bean
	Binding logBinding() {
		return BindingBuilder.bind(logQueue())
		.to(orderExchange())
		.with("order.#");
	}

	// DLQ
	@Bean
	DirectExchange orderDeadLetterExchange() {
		return new DirectExchange(properties.deadLetterExchange());
	}

	@Bean
	Queue mailDeadLetterQueue() {
		return QueueBuilder.durable(properties.mailDeadLetterQueue()).build();
	}

	@Bean
	Queue logDeadLetterQueue() {
		return QueueBuilder.durable(properties.logDeadLetterQueue()).build();
	}

	@Bean
	Binding mailDeadLetterBinding() {
		return BindingBuilder.bind(mailDeadLetterQueue())
			.to(orderDeadLetterExchange())
			.with(properties.mailDeadLetterQueue());
	}

	@Bean
	Binding logDeadLetterBinding() {
		return BindingBuilder.bind(logDeadLetterQueue())
			.to(orderDeadLetterExchange())
			.with(properties.logDeadLetterQueue());
	}
	
	// convert data
	@Bean
	MessageConverter jsonMessageConverter() {
		return new JacksonJsonMessageConverter("com.example.base.dto.order");
	}

	// send message
	@Bean
	RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setMessageConverter(messageConverter);

		return template;
	}

	// config retry for consumer
	@Bean
	RetryTemplate orderConsumerRetryTemplate() {
		RetryPolicy policy = RetryPolicy.builder().maxRetries(properties.consumerMaxRetries())
				.delay(Duration.ofMillis(properties.consumerRetryDelayMillis()))
				.multiplier(2).maxDelay(Duration.ofMillis(properties.consumerRetryDelayMillis() * 2))
				.includes(RuntimeException.class).excludes(InvalidOrderMessageException.class).build();

		return new RetryTemplate(policy);
	}
	
	@Bean
	SimpleRabbitListenerContainerFactory orderListenerContainerFactory(
			SimpleRabbitListenerContainerFactoryConfigurer configurer, ConnectionFactory connectionFactory) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		configurer.configure(factory, connectionFactory);
		factory.setMessageConverter(jsonMessageConverter());
		factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
		factory.setPrefetchCount(properties.consumerPrefetch());
		factory.setDefaultRequeueRejected(false);
		// Retry chỉ bao quanh nghiệp vụ trong listener, không retry thao tác ACK hoặc nhân đôi với Boot retry.
		factory.setAdviceChain();
		ConditionalRejectingErrorHandler errorHandler = new ConditionalRejectingErrorHandler();
		errorHandler.setRejectManual(true);
		// Message lỗi JSON được replay từ DLQ vẫn phải vào DLQ, không bỏ mất vì đã có x-death.
		errorHandler.setDiscardFatalsWithXDeath(false);
		factory.setErrorHandler(errorHandler);

		return factory;
	}

	private Queue orderQueue(String name, String deadLetterQueue) {
		QueueBuilder builder = QueueBuilder.durable(name);
		if (properties.declareDeadLetterArguments()) {
			builder.deadLetterExchange(properties.deadLetterExchange()).deadLetterRoutingKey(deadLetterQueue);
		}

		return builder.build();
	}

}

package com.example.base.config;

import java.time.Duration;

import com.example.base.config.property.OrderProperties;
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
	private final OrderProperties orderProperties;

	public RabbitConfig(RabbitProperties properties, OrderProperties orderProperties) {
		this.properties = properties;
		this.orderProperties = orderProperties;
	}

	@Bean
	TopicExchange orderExchange() {
		return new TopicExchange(properties.orderEvent().exchange());
	}

	@Bean
	Queue mailQueue() {
		return orderQueue(properties.orderEvent().mailQueue(), properties.orderEvent().mailDeadLetterQueue());
	}

	@Bean
	Queue logQueue() {
		return orderQueue(properties.orderEvent().logQueue(), properties.orderEvent().logDeadLetterQueue());
	}

	@Bean
	Binding mailBinding() {
		return BindingBuilder.bind(mailQueue())
			.to(orderExchange())
			.with(properties.orderEvent().routingKeyPaid());
	}

	@Bean
	Binding logBinding() {
		return BindingBuilder.bind(logQueue())
		.to(orderExchange())
		.with("order.#");
	}

	// DLQ
	@Bean
	DirectExchange orderDeadLetterExchange() {
		return new DirectExchange(properties.orderEvent().deadLetterExchange());
	}

	@Bean
	Queue mailDeadLetterQueue() {
		return QueueBuilder.durable(properties.orderEvent().mailDeadLetterQueue())
				.build();
	}

	@Bean
	Queue logDeadLetterQueue() {
		return QueueBuilder.durable(properties.orderEvent().logDeadLetterQueue())
				.build();
	}

	@Bean
	Binding mailDeadLetterBinding() {
		return BindingBuilder.bind(mailDeadLetterQueue())
			.to(orderDeadLetterExchange())
			.with(properties.orderEvent().mailDeadLetterQueue());
	}

	@Bean
	Binding logDeadLetterBinding() {
		return BindingBuilder.bind(logDeadLetterQueue())
			.to(orderDeadLetterExchange())
			.with(properties.orderEvent().logDeadLetterQueue());
	}

	@Bean
	DirectExchange orderHoldExchange() {
		return new DirectExchange(properties.orderExpiry().holdExchange());
	}

	@Bean
	Queue orderHoldQueue() {
		return QueueBuilder.durable(properties.orderExpiry().holdQueue())
				.deadLetterExchange(properties.orderEvent().deadLetterExchange())
				.deadLetterRoutingKey(properties.orderExpiry().routingKeyExpired())
				.ttl((int) Duration.ofMinutes(orderProperties.holdMinutes()).toMillis())
				.build();
	}

	@Bean
	Binding orderHoldBinding() {
		return BindingBuilder.bind(orderHoldQueue())
				.to(orderHoldExchange())
				.with(properties.orderExpiry().routingKeyHold());
	}

	@Bean
	Queue orderExpireQueue() {
		return orderQueue(properties.orderExpiry().expireQueue(), properties.orderExpiry().expireDeadLetterQueue());
	}

	@Bean
	Binding orderExpireBinding() {
		return BindingBuilder.bind(orderExpireQueue())
				.to(orderDeadLetterExchange())
				.with(properties.orderExpiry().routingKeyExpired());
	}

	@Bean
	Queue orderExpireDeadLetterQueue() {
		return QueueBuilder.durable(properties.orderExpiry().expireDeadLetterQueue())
				.build();
	}

	@Bean
	Binding orderExpireDeadLetterBinding() {
		return BindingBuilder.bind(orderExpireDeadLetterQueue())
				.to(orderDeadLetterExchange())
				.with(properties.orderExpiry().expireDeadLetterQueue());
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
		RetryPolicy policy = RetryPolicy.builder().maxRetries(properties.listener().maxRetries())
				.delay(Duration.ofMillis(properties.listener().retryDelayMillis()))
				.multiplier(2).maxDelay(Duration.ofMillis(properties.listener().retryDelayMillis() * 2))
				.includes(RuntimeException.class)
				.excludes(InvalidOrderMessageException.class)
				.build();

		return new RetryTemplate(policy);
	}

	@Bean
	SimpleRabbitListenerContainerFactory orderListenerContainerFactory(
			SimpleRabbitListenerContainerFactoryConfigurer configurer,
			ConnectionFactory connectionFactory) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		configurer.configure(factory, connectionFactory);

		factory.setMessageConverter(jsonMessageConverter());
		factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
		factory.setPrefetchCount(properties.listener().prefetch());
		factory.setDefaultRequeueRejected(false);

		factory.setAdviceChain();

		ConditionalRejectingErrorHandler errorHandler = new ConditionalRejectingErrorHandler();
		errorHandler.setRejectManual(true);
		errorHandler.setDiscardFatalsWithXDeath(false);
		factory.setErrorHandler(errorHandler);

		return factory;
	}

	private Queue orderQueue(String name, String deadLetterQueue) {
		QueueBuilder builder = QueueBuilder.durable(name);
		if (properties.listener().declareDeadLetterArguments()) {
			builder.deadLetterExchange(properties.orderEvent().deadLetterExchange())
				.deadLetterRoutingKey(deadLetterQueue);
		}

		return builder.build();
	}

}

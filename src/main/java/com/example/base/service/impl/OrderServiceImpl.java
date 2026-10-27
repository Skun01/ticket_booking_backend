package com.example.base.service.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

import com.example.base.config.property.OrderProperties;
import com.example.base.domain.Event;
import com.example.base.domain.Order;
import com.example.base.domain.OrderItem;
import com.example.base.domain.Payment;
import com.example.base.domain.Ticket;
import com.example.base.domain.TicketType;
import com.example.base.domain.User;
import com.example.base.domain.enums.OrderStatus;
import com.example.base.domain.enums.UserStatus;
import com.example.base.dto.common.PageResponse;
import com.example.base.dto.order.CreateOrderRequest;
import com.example.base.dto.order.OrderMessage;
import com.example.base.dto.order.OrderResponse;
import com.example.base.dto.order.OrderSummaryResponse;
import com.example.base.dto.payment.PayOrderRequest;
import com.example.base.exception.AppException;
import com.example.base.exception.EventErrorCode;
import com.example.base.exception.OrderErrorCode;
import com.example.base.exception.OrderExpiredException;
import com.example.base.exception.PaymentDeclinedException;
import com.example.base.exception.TicketTypeErrorCode;
import com.example.base.exception.UserErrorCode;
import com.example.base.repository.EventRepository;
import com.example.base.repository.OrderRepository;
import com.example.base.repository.PaymentRepository;
import com.example.base.repository.TicketRepository;
import com.example.base.repository.TicketTypeRepository;
import com.example.base.repository.UserRepository;
import com.example.base.service.OrderService;
import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

	private final OrderRepository orderRepository;
	private final UserRepository userRepository;
	private final EventRepository eventRepository;
	private final PaymentRepository paymentRepository;
	private final TicketRepository ticketRepository;
	private final TicketTypeRepository ticketTypeRepository;
	private final OrderProperties properties;
	private final ApplicationEventPublisher eventPublisher;

	@Override
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public OrderResponse create(Long customerId, CreateOrderRequest request) {
		User customer = userRepository.findById(customerId)
				.orElseThrow(() -> new AppException(UserErrorCode.USER_NOT_FOUND));
		requireActiveCustomer(customer);

		TicketType ticketType = reserveStock(request.ticketTypeId(), request.quantity());
		Instant expiresAt = Instant.now().plus(properties.holdMinutes(), ChronoUnit.MINUTES);
		if (ticketType.getEvent().getStartTime().isBefore(expiresAt)) {
			expiresAt = ticketType.getEvent().getStartTime();
		}
		Order order = new Order(customer, ticketType, request.quantity(), expiresAt);
		orderRepository.saveAndFlush(order);

		return OrderResponse.from(order);
	}

	@Override
	@Transactional(noRollbackFor = OrderExpiredException.class)
	public OrderResponse pay(Long customerId, Long orderId, PayOrderRequest request) {
		Order order = findLockedOwn(customerId, orderId);
		if (order.getStatus() == OrderStatus.PAID) {
			return OrderResponse.from(order);
		}
		if (order.getStatus() == OrderStatus.EXPIRED) {
			throw new OrderExpiredException();
		}
		order.requirePending();
		expireBeforePayment(order, Instant.now());
		requireActiveCustomer(order.getUser());
		for (OrderItem item : order.getItems()) {
			Event event = eventRepository.findBookingById(item.getTicketType().getEvent().getId())
					.orElseThrow(() -> new AppException(EventErrorCode.EVENT_NOT_FOUND));
			Instant now = Instant.now();
			expireBeforePayment(order, now);
			event.requireBookable(now);
		}
		Instant paidAt = Instant.now();
		expireBeforePayment(order, paidAt);

		order.markPaid(paidAt);
		if (request.simulateFailure()) {
			throw new PaymentDeclinedException(orderId, request.method(), order.getTotalAmount());
		}

		for (OrderItem item : order.getItems()) {
			for (int i = 0; i < item.getQuantity(); i++) {
				ticketRepository.save(new Ticket(item));
			}
		}
		paymentRepository.save(Payment.success(order, request.method(), paidAt));
		eventPublisher.publishEvent(OrderMessage.from(order, paidAt));

		return OrderResponse.from(order);
	}

	@Override
	@Transactional
	public OrderResponse cancel(Long customerId, Long orderId) {
		Order order = findLockedOwn(customerId, orderId);
		if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.EXPIRED) {
			return OrderResponse.from(order);
		}
		order.requirePending();
		Instant now = Instant.now();
		if (order.expire(now)) {
			releaseStock(order);

			return OrderResponse.from(order);
		}
		order.cancel();
		releaseStock(order);
		eventPublisher.publishEvent(OrderMessage.from(order, now));

		return OrderResponse.from(order);
	}

	@Override
	public PageResponse<OrderSummaryResponse> listOwn(Long customerId, Pageable pageable) {
		return PageResponse.from(
				orderRepository.findByUserId(customerId, pageable)
						.map(OrderSummaryResponse::from)
		);
	}

	@Override
	public OrderResponse getOwn(Long customerId, Long orderId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new AppException(OrderErrorCode.ORDER_NOT_FOUND));
		requireOwner(order, customerId);

		return OrderResponse.from(order);
	}

	@Override
	public List<Long> findExpiredIds(Instant cutoff, int limit) {
		return orderRepository.findExpiredIds(OrderStatus.PENDING, cutoff,
				PageRequest.of(0, limit));
	}

	@Override
	@Transactional
	public boolean expire(Long orderId) {
		Order order = orderRepository.findLockedById(orderId).orElse(null);
		if (order == null || !order.expire(Instant.now())) {
			return false;
		}
		releaseStock(order);

		return true;
	}

	private TicketType reserveStock(Long ticketTypeId, int quantity) {
		Long eventId = ticketTypeRepository.findEventIdById(ticketTypeId)
				.orElseThrow(() -> new AppException(TicketTypeErrorCode.TICKET_TYPE_NOT_FOUND));

		eventRepository.findBookingById(eventId)
			.orElseThrow(() -> new AppException(EventErrorCode.EVENT_NOT_FOUND));
			
		TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
				.orElseThrow(() -> new AppException(TicketTypeErrorCode.TICKET_TYPE_NOT_FOUND));

		ticketType.requireBookable(quantity, Instant.now());
		if (ticketTypeRepository.reserve(ticketTypeId, quantity) != 1) {
			throw new AppException(OrderErrorCode.SOLD_OUT);
		}
		
		ticketType.requireBookable(quantity, Instant.now());

		return ticketType;
	}

	private void releaseStock(Order order) {
		for (OrderItem item : order.getItems().stream()
				.sorted(Comparator.comparing(value -> value.getTicketType().getId())).toList()) {
			if (ticketTypeRepository.releaseSold(item.getTicketType().getId(), item.getQuantity()) != 1) {
				throw new AppException(OrderErrorCode.INVENTORY_CONFLICT);
			}
		}
	}

	private Order findLockedOwn(Long customerId, Long orderId) {
		Order order = orderRepository.findLockedById(orderId)
				.orElseThrow(() -> new AppException(OrderErrorCode.ORDER_NOT_FOUND));
		requireOwner(order, customerId);

		return order;
	}

	private void requireOwner(Order order, Long customerId) {
		if (!order.isOwnedBy(customerId)) {
			throw new AppException(OrderErrorCode.ORDER_NOT_OWNED);
		}
	}

	private void requireActiveCustomer(User customer) {
		if (customer.getStatus() != UserStatus.ACTIVE) {
			throw new AppException(OrderErrorCode.CUSTOMER_INACTIVE);
		}
	}

	private void expireBeforePayment(Order order, Instant now) {
		if (order.expire(now)) {
			releaseStock(order);
			throw new OrderExpiredException();
		}
	}

}

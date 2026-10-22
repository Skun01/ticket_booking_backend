package com.example.base.service.impl;

import java.util.List;

import com.example.base.domain.Order;
import com.example.base.domain.enums.OrderStatus;
import com.example.base.dto.notification.PaidOrderMailData;
import com.example.base.dto.notification.TicketMailData;
import com.example.base.dto.order.OrderMessage;
import com.example.base.exception.InvalidOrderMessageException;
import com.example.base.notification.OrderMessageValidator;
import com.example.base.repository.OrderRepository;
import com.example.base.repository.TicketRepository;
import com.example.base.service.OrderMailService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderMailServiceImpl implements OrderMailService {

	private final OrderRepository orderRepository;
	private final TicketRepository ticketRepository;
	private final OrderMessageValidator messageValidator;

	@Override
	public PaidOrderMailData loadPaidOrder(OrderMessage message) {
		Order order = orderRepository.findById(message.orderId()).orElseThrow(InvalidOrderMessageException::new);
		messageValidator.requireMatches(message, order);
		if (order.getStatus() != OrderStatus.PAID) {
			throw new InvalidOrderMessageException();
		}
		List<TicketMailData> tickets = ticketRepository.findByOrderItemOrderIdOrderByIdAsc(order.getId()).stream()
				.map(TicketMailData::from).toList();
		long expectedQuantity = order.getItems().stream().mapToLong(item -> item.getQuantity()).sum();
		if (tickets.isEmpty() || tickets.size() != expectedQuantity) {
			throw new InvalidOrderMessageException();
		}

		return new PaidOrderMailData(order.getUser().getEmail(), order.getUser().getFullName(), order.getOrderCode(),
				order.getTotalAmount(), tickets);
	}

}

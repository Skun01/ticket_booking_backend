package com.example.base.service.impl;

import java.time.Instant;
import java.util.Comparator;

import com.example.base.domain.Order;
import com.example.base.domain.OrderItem;
import com.example.base.domain.TicketType;
import com.example.base.exception.AppException;
import com.example.base.exception.EventErrorCode;
import com.example.base.exception.OrderErrorCode;
import com.example.base.exception.TicketTypeErrorCode;
import com.example.base.repository.EventRepository;
import com.example.base.repository.TicketTypeRepository;
import com.example.base.service.OrderStockService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class OrderStockServiceImpl implements OrderStockService {

	private final TicketTypeRepository ticketTypeRepository;
	private final EventRepository eventRepository;

	@Override
	public TicketType reserve(Long ticketTypeId, int quantity) {
		Long eventId = ticketTypeRepository.findEventIdById(ticketTypeId)
				.orElseThrow(() -> new AppException(TicketTypeErrorCode.TICKET_TYPE_NOT_FOUND));
		// Khóa chia sẻ cho phép nhiều khách mua đồng thời nhưng chặn organizer đổi trạng thái giữa chừng.
		eventRepository.findBookingById(eventId)
				.orElseThrow(() -> new AppException(EventErrorCode.EVENT_NOT_FOUND));
		TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
				.orElseThrow(() -> new AppException(TicketTypeErrorCode.TICKET_TYPE_NOT_FOUND));
		ticketType.requireBookable(quantity, Instant.now());
		if (ticketTypeRepository.reserve(ticketTypeId, quantity) != 1) {
			throw new AppException(OrderErrorCode.SOLD_OUT);
		}
		// Có thể đã chờ khóa kho lâu; kiểm tra lại giờ bán sau UPDATE, sai thì rollback cả lượng giữ.
		ticketType.requireBookable(quantity, Instant.now());

		return ticketType;
	}

	@Override
	public void release(Order order) {
		for (OrderItem item : order.getItems().stream()
				.sorted(Comparator.comparing(value -> value.getTicketType().getId())).toList()) {
			if (ticketTypeRepository.releaseSold(item.getTicketType().getId(), item.getQuantity()) != 1) {
				throw new AppException(OrderErrorCode.INVENTORY_CONFLICT);
			}
		}
	}

}

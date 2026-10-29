package com.example.base.service.impl;

import java.time.Instant;
import java.util.List;

import com.example.base.config.property.PayOSProperties;
import com.example.base.domain.Order;
import com.example.base.domain.enums.OrderStatus;
import com.example.base.domain.enums.PaymentMethod;
import com.example.base.domain.enums.UserStatus;
import com.example.base.dto.order.OrderResponse;
import com.example.base.dto.payment.PayOSCheckoutResponse;
import com.example.base.exception.AppException;
import com.example.base.exception.OrderErrorCode;
import com.example.base.exception.OrderExpiredException;
import com.example.base.exception.PaymentErrorCode;
import com.example.base.repository.OrderRepository;
import com.example.base.service.OrderService;
import com.example.base.service.PayOSPaymentService;
import lombok.RequiredArgsConstructor;
import vn.payos.PayOS;
import vn.payos.exception.PayOSException;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.v2.paymentRequests.PaymentLinkStatus;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PayOSPaymentServiceImpl implements PayOSPaymentService {

	private final PayOS payOS;
	private final PayOSProperties payOSProperties;
	private final OrderRepository orderRepository;
	private final OrderService orderService;

	@Override
	public PayOSCheckoutResponse createPaymentLink(Long customerId, Long orderId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new AppException(OrderErrorCode.ORDER_NOT_FOUND));

		if (!order.getUser().getId().equals(customerId)) {
			throw new AppException(OrderErrorCode.ORDER_NOT_OWNED);
		}
		if (order.getUser().getStatus() != UserStatus.ACTIVE) {
			throw new AppException(OrderErrorCode.CUSTOMER_INACTIVE);
		}
		if (order.getStatus() == OrderStatus.EXPIRED || order.getExpiresAt().isBefore(Instant.now())) {
			throw new OrderExpiredException();
		}
		order.requirePending();

		String description = "Don hang " + order.getId();
		if (description.length() > 25) {
			description = description.substring(0, 25);
		}

		List<PaymentLinkItem> items = order.getItems().stream()
				.map(item -> PaymentLinkItem.builder()
						.name(item.getTicketType().getName())
						.quantity(item.getQuantity())
						.price(item.getUnitPrice().longValue())
						.build())
				.toList();

		CreatePaymentLinkRequest request = CreatePaymentLinkRequest.builder()
				.orderCode(order.getId())
				.amount(order.getTotalAmount().longValue())
				.description(description)
				.items(items)
				.returnUrl(payOSProperties.returnUrl())
				.cancelUrl(payOSProperties.cancelUrl())
				.buyerName(order.getUser().getFullName())
				.buyerEmail(order.getUser().getEmail())
				.buyerPhone(order.getUser().getPhone())
				.expiredAt(order.getExpiresAt().getEpochSecond())
				.build();

		CreatePaymentLinkResponse response;
		try {
			response = payOS.paymentRequests().create(request);
		}
		catch (PayOSException ex) {
			try {
				// Hủy link cũ nếu đã tồn tại và tạo lại link mới
				payOS.paymentRequests().cancel(order.getId(), "Recreate payment link");
				response = payOS.paymentRequests().create(request);
			}
			catch (Exception retryEx) {
				throw new AppException(PaymentErrorCode.PAYMENT_LINK_CREATION_FAILED);
			}
		}

		String status = response.getStatus() != null ? response.getStatus().name() : "PENDING";

		return new PayOSCheckoutResponse(order.getId(), response.getOrderCode(), response.getCheckoutUrl(),
				response.getQrCode(), response.getPaymentLinkId(), response.getAmount(), status);
	}

	@Override
	public void handleWebhook(Webhook webhook) {
		WebhookData data;
		try {
			data = payOS.webhooks().verify(webhook);
		}
		catch (Exception ex) {
			throw new AppException(PaymentErrorCode.INVALID_PAYMENT_WEBHOOK);
		}

		if (data == null || data.getOrderCode() == null) {
			throw new AppException(PaymentErrorCode.INVALID_PAYMENT_WEBHOOK);
		}

		if ("00".equals(data.getCode())) {
			orderService.completePayment(data.getOrderCode(), PaymentMethod.PAYOS, data.getReference(), Instant.now());
		}
	}

	@Override
	public OrderResponse syncPayment(Long customerId, Long orderId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new AppException(OrderErrorCode.ORDER_NOT_FOUND));

		if (!order.getUser().getId().equals(customerId)) {
			throw new AppException(OrderErrorCode.ORDER_NOT_OWNED);
		}
		if (order.getStatus() == OrderStatus.PAID) {
			return OrderResponse.from(order);
		}

		if (order.getStatus() == OrderStatus.PENDING) {
			try {
				PaymentLink link = payOS.paymentRequests().get(orderId);
				if (link != null && link.getStatus() == PaymentLinkStatus.PAID) {
					String ref = (link.getTransactions() != null && !link.getTransactions().isEmpty())
							? link.getTransactions().get(0).getReference()
							: null;

					return orderService.completePayment(orderId, PaymentMethod.PAYOS, ref, Instant.now());
				}
			}
			catch (Exception ignored) {
				// Bỏ qua lỗi PayOS khi sync để trả về trạng thái đơn hiện tại
			}
		}

		return OrderResponse.from(order);
	}

}

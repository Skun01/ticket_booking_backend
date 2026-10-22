package com.example.base.controller;

import java.net.URI;

import com.example.base.annotation.CurrentUser;
import com.example.base.annotation.IsCustomer;
import com.example.base.domain.enums.PaymentMethod;
import com.example.base.dto.common.PageResponse;
import com.example.base.dto.order.CreateOrderRequest;
import com.example.base.dto.order.OrderResponse;
import com.example.base.dto.order.OrderSummaryResponse;
import com.example.base.dto.payment.PayOrderRequest;
import com.example.base.service.OrderPaymentService;
import com.example.base.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@IsCustomer
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;
	private final OrderPaymentService orderPaymentService;

	@Operation(summary = "Đặt vé và giữ chỗ")
	@PostMapping
	public ResponseEntity<OrderResponse> create(@CurrentUser Jwt jwt, @Valid @RequestBody CreateOrderRequest request) {
		OrderResponse response = orderService.create(Long.valueOf(jwt.getSubject()), request);

		return ResponseEntity.created(URI.create("/api/orders/" + response.id())).body(response);
	}

	@Operation(summary = "Thanh toán giả lập đơn vé")
	@PostMapping("/{id}/pay")
	public OrderResponse pay(@CurrentUser Jwt jwt, @PathVariable @Positive Long id,
			@Valid @RequestBody(required = false) PayOrderRequest request) {
		PayOrderRequest payment = request == null ? new PayOrderRequest(PaymentMethod.MOCK, false) : request;

		return orderPaymentService.pay(Long.valueOf(jwt.getSubject()), id, payment);
	}

	@Operation(summary = "Hủy đơn vé chưa thanh toán")
	@PostMapping("/{id}/cancel")
	public OrderResponse cancel(@CurrentUser Jwt jwt, @PathVariable @Positive Long id) {
		return orderService.cancel(Long.valueOf(jwt.getSubject()), id);
	}

	@Operation(summary = "Danh sách đơn vé của tôi")
	@GetMapping("/my")
	public PageResponse<OrderSummaryResponse> listOwn(@CurrentUser Jwt jwt,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
		return orderService.listOwn(Long.valueOf(jwt.getSubject()),
				PageRequest.of(page, size, Sort.by("createdAt", "id").descending()));
	}

	@Operation(summary = "Chi tiết đơn vé của tôi")
	@GetMapping("/{id}")
	public OrderResponse getOwn(@CurrentUser Jwt jwt, @PathVariable @Positive Long id) {
		return orderService.getOwn(Long.valueOf(jwt.getSubject()), id);
	}

}

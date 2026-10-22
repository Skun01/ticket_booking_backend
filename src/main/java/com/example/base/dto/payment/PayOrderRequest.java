package com.example.base.dto.payment;

import com.example.base.domain.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record PayOrderRequest(@NotNull PaymentMethod method, boolean simulateFailure) {
}

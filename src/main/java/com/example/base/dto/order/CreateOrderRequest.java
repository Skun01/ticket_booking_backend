package com.example.base.dto.order;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateOrderRequest(@NotNull @Positive Long ticketTypeId, @Positive @Max(100) int quantity) {
}

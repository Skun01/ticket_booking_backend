package com.example.base.dto.event;

import com.example.base.domain.enums.EventStatus;
import jakarta.validation.constraints.NotNull;

public record EventStatusRequest(@NotNull EventStatus status) {
}

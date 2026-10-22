package com.example.base.dto.ticket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CheckInRequest(@NotBlank @Size(max = 40) String ticketCode) {
}

package ua.dp.maxym.demo5.dto;

import org.springframework.data.annotation.Id;

import java.util.UUID;

public record PaymentUserCredit(@Id UUID id, String user, Double amount) {
}

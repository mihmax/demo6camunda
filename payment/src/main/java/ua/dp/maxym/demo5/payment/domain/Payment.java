package ua.dp.maxym.demo5.payment.domain;

import org.springframework.data.annotation.Id;

import java.util.UUID;

public record Payment(@Id UUID id, String user, Double amount) {
}

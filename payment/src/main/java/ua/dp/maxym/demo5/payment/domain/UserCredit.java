package ua.dp.maxym.demo5.payment.domain;

import org.springframework.data.annotation.Id;

public record UserCredit(@Id String user, Double credit) {
}

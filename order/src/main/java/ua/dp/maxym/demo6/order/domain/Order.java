package ua.dp.maxym.demo6.order.domain;

import org.springframework.data.annotation.Id;

import java.util.UUID;

public record Order(
        @Id UUID id,
        OrderStatus status,
        String statusReason,
        String user,
        String goods,
        Integer quantity,
        Double price
) {
}

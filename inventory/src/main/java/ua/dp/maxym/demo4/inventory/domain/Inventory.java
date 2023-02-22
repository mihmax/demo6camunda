package ua.dp.maxym.demo4.inventory.domain;

import org.springframework.data.annotation.Id;

public record Inventory(@Id String goods, Integer quantity, Double pricePerItem) {
}

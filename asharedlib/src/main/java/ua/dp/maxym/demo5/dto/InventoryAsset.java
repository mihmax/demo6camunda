package ua.dp.maxym.demo5.dto;

import org.springframework.data.annotation.Id;

import java.util.UUID;

public record InventoryAsset(@Id UUID id, String goods, Integer quantity) {
}

package ua.dp.maxym.demo5.dto;

import org.springframework.data.annotation.Id;

import java.util.UUID;

public record ShipmentAddress(@Id UUID id, String user, String address) {
}

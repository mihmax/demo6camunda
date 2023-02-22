package ua.dp.maxym.demo5.shipment.domain;

import org.springframework.data.annotation.Id;

public record UserAddress(@Id String user, String address) {
}

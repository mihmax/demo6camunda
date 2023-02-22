package ua.dp.maxym.demo6.order.domain;

import org.springframework.data.annotation.Id;

import java.util.Date;

public record Log(@Id Date timestamp, String logMessage) {
}


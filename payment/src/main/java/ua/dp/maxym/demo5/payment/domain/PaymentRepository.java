package ua.dp.maxym.demo5.payment.domain;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface PaymentRepository extends MongoRepository<Payment, UUID> {

}

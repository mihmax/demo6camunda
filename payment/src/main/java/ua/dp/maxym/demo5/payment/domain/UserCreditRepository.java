package ua.dp.maxym.demo5.payment.domain;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserCreditRepository extends MongoRepository<UserCredit, String> {
    UserCredit findByUser(String user);
}

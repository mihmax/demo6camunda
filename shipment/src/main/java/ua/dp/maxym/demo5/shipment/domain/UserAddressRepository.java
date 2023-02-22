package ua.dp.maxym.demo5.shipment.domain;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserAddressRepository extends MongoRepository<UserAddress, String> {
    UserAddress findByUser(String user);
}

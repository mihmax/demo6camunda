package ua.dp.maxym.demo5.shipment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ua.dp.maxym.demo5.shipment.domain.UserAddress;
import ua.dp.maxym.demo5.shipment.domain.UserAddressRepository;


@SpringBootApplication
public class Demo5ShipmentApplication {

    public Demo5ShipmentApplication(UserAddressRepository userAddressRepository) {
        userAddressRepository.deleteAll();
        userAddressRepository.insert(new UserAddress("user2", "Dnipro, Talalikhina Str 1 / 6"));
    }

    public static void main(String[] args) {
        SpringApplication.run(Demo5ShipmentApplication.class, args);
    }

}
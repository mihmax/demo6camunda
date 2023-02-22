package ua.dp.maxym.demo5.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import ua.dp.maxym.demo5.payment.domain.UserCredit;
import ua.dp.maxym.demo5.payment.domain.UserCreditRepository;


@SpringBootApplication
public class Demo5PaymentApplication {

    public Demo5PaymentApplication(UserCreditRepository creditRepository) {
        // Initializing user credit upon each start
        creditRepository.save(new UserCredit("user1", 1000.0));
        creditRepository.save(new UserCredit("user2", 500.0));
    }

    public static void main(String[] args) {
        SpringApplication.run(Demo5PaymentApplication.class, args);
    }
}
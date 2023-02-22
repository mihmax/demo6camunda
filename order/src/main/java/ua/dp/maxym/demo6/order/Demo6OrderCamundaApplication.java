package ua.dp.maxym.demo6.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ua.dp.maxym.demo6.order.domain.LogRepository;
import ua.dp.maxym.demo6.order.domain.OrderRepository;

import java.sql.SQLException;


@SpringBootApplication
public class Demo6OrderCamundaApplication {

    private final LogRepository logger;

    public Demo6OrderCamundaApplication(LogRepository logger, OrderRepository orderRepository) {
        this.logger = logger;
        logger.deleteAll();
        orderRepository.deleteAll();
        logger.log("Application started");
    }

    public static void main(String[] args) throws SQLException {
        SpringApplication.run(Demo6OrderCamundaApplication.class, args);

        // Start H2 server to be able to connect to database from the outside
        // Server.createTcpServer(new String[]{"-tcpPort", "8092", "-tcpAllowOthers"}).start();
    }

}
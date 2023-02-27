package ua.dp.maxym.demo4.inventory;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import ua.dp.maxym.demo4.inventory.domain.Inventory;
import ua.dp.maxym.demo4.inventory.domain.InventoryRepository;


@SpringBootApplication
@ComponentScan(basePackages = {"ua.dp.maxym.demo4"})
public class Demo4InventoryApplication {

    private final InventoryRepository inventoryRepository;

    public Demo4InventoryApplication(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @PostConstruct
    public void init() {
        inventoryRepository.deleteAll();
        inventoryRepository.insert(new Inventory("item1", 10, 100.0));
        inventoryRepository.insert(new Inventory("item2", 5, 10.0));
    }

    public static void main(String[] args) {
        SpringApplication.run(Demo4InventoryApplication.class, args);
    }

}
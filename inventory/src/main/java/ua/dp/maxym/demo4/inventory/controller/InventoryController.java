package ua.dp.maxym.demo4.inventory.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.dp.maxym.demo4.inventory.domain.Inventory;
import ua.dp.maxym.demo4.inventory.domain.InventoryRepository;

import java.io.IOException;
import java.util.stream.Collectors;

@RestController
public class InventoryController {

    @Autowired
    private InventoryRepository inventoryRepository;

    @GetMapping({"/", "/list"})
    public String list() {
        return String.format("""
                                     Contents of inventory:
                                     <br/><br/> 
                                     %1$s
                                     """, inventoryRepository.findAll().stream().map(Object::toString)
                                                             .collect(Collectors.joining("<br/>")));
    }

    @GetMapping("/get")
    public Inventory get(String goods, HttpServletResponse response) throws IOException {
        if (goods == null || goods.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                               String.format("Error, invalid parameters. Expecting goods (got %s).", goods));
            return null;
        }
        var inventory = inventoryRepository.findById(goods);
        if (inventory.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, String.format("Error, goods %s not found", goods));
            return null;
        }

        return inventory.get();
    }

}

package ua.dp.maxym.demo5.shipment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.dp.maxym.demo5.shipment.domain.UserAddressRepository;

import java.util.stream.Collectors;

@RestController
public class ShipmentController {

    @Autowired
    private UserAddressRepository userAddressRepository;

    @GetMapping({"/", "/list"})
    public String list() {
        return String.format("""
            User addresses:
            <br/><br/> 
            %1$s
            """, userAddressRepository.findAll().stream().map(Object::toString)
                .collect(Collectors.joining("<br/>")));
    }

}

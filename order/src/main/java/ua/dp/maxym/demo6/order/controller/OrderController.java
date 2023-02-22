package ua.dp.maxym.demo6.order.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.dp.maxym.demo6.order.camunda.MainOrderService;
import ua.dp.maxym.demo6.order.domain.LogRepository;
import ua.dp.maxym.demo6.order.domain.Order;
import ua.dp.maxym.demo6.order.domain.OrderRepository;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class OrderController {

    private final LogRepository logRepository;
    private final OrderRepository orderRepository;

    private final MainOrderService orderService;

    public OrderController(LogRepository logRepository, OrderRepository orderRepository, MainOrderService orderService) {
        this.logRepository = logRepository;
        this.orderRepository = orderRepository;
        this.orderService = orderService;
    }

    @GetMapping("/")
    public String index() {
        return """
                <!DOCTYPE html>
<html lang="en">
<head>
    <title>Demo5 with Temporal</title>
</head>
<body>
<h3>Demo5 with Temporal</h3>
<p>SAGA Orchestration using <a href="https://temporal.io">Temporal.io</a></p>
<ul>
    <li><a href="/list">List all executed orders</a></li>
    <li><a href="/log">Application log</a></li>
</ul>

<form action="/order" method="get">
    <table>
        <thead>
        <tr>
            <td><b>Order</b></td>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td><label for="user">User:</label></td>
            <td>
                <select id="user" name="user">
                    <option value="user1">User 1</option>
                    <option value="user2">User 2</option>
                </select>
            </td>
        </tr>
        <tr>
            <td><label for="goods">Goods item:</label></td>
            <td>
                <select id="goods" name="goods" onchange="updateAmount()">
                    <option value="item1">Item 1</option>
                    <option value="item2">Item 2</option>
                </select>
            </td>
        </tr>
        <tr>
            <td><label for="quantity">Quantity:</label></td>
            <td><input id="quantity" name="quantity" type="text" onchange="updateAmount()"/></td>
        </tr>
        <tr>
            <td><input id="submit" type="submit"/></td>
        </tr>
        </tbody>
    </table>
</form>
</body>
</html>
                """;
    }
    @GetMapping("/list")
    public String list() {
        return String.format("""
                                     Order list:
                                     <br/><br/>
                                     %1$s
                                     <br/><br/>
                                     See also <a href="/log">Log</a>
                                     """, orderRepository.findAll().stream().map(Object::toString)
                                                         .collect(Collectors.joining("<br/>")));
    }

    @GetMapping("/log")
    public String log() {
        return String.format("""
                                     Log messages:
                                     <br/><br/>
                                     %1$s
                                     <br/><br/>
                                     See also <a href="/list">Order List</a>
                                     """, logRepository.findAll().stream().map(Object::toString)
                                                       .collect(Collectors.joining("<br/>")));
    }

    @GetMapping("/order")
    public String order(String user, String goods, Integer quantity) {
        UUID orderId = orderService.order(user, goods, quantity);
        return String.format("""
                                     Initiating order for
                                     <ul>
                                         <li>User %s</li>
                                         <li>Goods %s</li>
                                         <li>Quantity %s</li>
                                     </ul>
                                     <br>
                                     To check status, query
                                     <a href="/status?id=%4$s" target="_blank">Order %4$s status</a>
                                     """, user, goods, quantity, orderId);
    }

    @GetMapping("/status")
    public String status(String id) {
        UUID orderId = UUID.fromString(id);
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            return switch (order.status()) {
                case NOT_STARTED, INITIATED, SUCCEEDED -> order.status().toString();
                case REVERT_INITIATED, FAILED ->
                        String.format("%s with reason %s", order.status(), order.statusReason());
            };
        } else {
            return """
                    Order with id %s not found.
                    <br/><br/>
                    See <a href="/">Order List</a> and <a href="/log">Application Log</a>
                    """;
        }
    }
}

package ua.dp.maxym.demo6.order.camunda.activity;

import org.apache.tomcat.util.json.JSONParser;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ua.dp.maxym.demo6.order.camunda.MainOrderService;
import ua.dp.maxym.demo6.order.domain.LogRepository;
import ua.dp.maxym.demo6.order.domain.OrderRepository;
import ua.dp.maxym.demo6.order.domain.OrderStatus;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.SECONDS;

@Component
public class InventoryGetPrice implements JavaDelegate {

    @Autowired
    private LogRepository logger;
    @Autowired
    private OrderRepository orderRepository;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        logger.log("InventoryGetPrice called with %s", execution.getVariables());
        String goods = (String) execution.getVariable(MainOrderService.VARIABLE_GOODS);

        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(new URI(String.format("http://localhost:8084/get?goods=%s", goods)))
                                         .GET()
                                         .timeout(Duration.of(10, SECONDS))
                                         .build();
        HttpClient client = HttpClient.newHttpClient();
        logger.log("InventoryGetPrice calling inventory service for price of goods %s", goods);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        switch (response.statusCode()) {
            case HttpURLConnection.HTTP_BAD_REQUEST,
                    HttpURLConnection.HTTP_NOT_FOUND:
                // Expected (business) errors
                logger.log("ERROR! InventoryGetPrice received expected error code %s, message %s",
                           response.statusCode(), response.body());
                throw new BpmnError(MainOrderService.DO_NOT_RETRY,
                        String.format("Business Validation error in Inventory service %s", response.body()));
            case HttpURLConnection.HTTP_OK:
                // Success, getting UUID
                var inventoryStr = response.body();
                logger.log("InventoryGetPrice got response from inventory service %s", inventoryStr);
                String priceStr = new JSONParser(inventoryStr).parseObject().get("pricePerItem").toString();
                Double price = Double.parseDouble(priceStr);
                execution.setVariable(MainOrderService.VARIABLE_PRICE_PER_ITEM, price);
                UUID orderId = (UUID) execution.getVariable(MainOrderService.VARIABLE_ORDER_ID);

                Integer quantity = (Integer) execution.getVariable(MainOrderService.VARIABLE_QUANTITY);
                orderRepository.updatePrice(orderId, price * quantity);

                logger.log("InventoryGetPrice saved price per item %s", price);
                break;
            default:
                // Unexpected error
                logger.log("ERROR! InventoryGetPrice received unexpected error code %s, message %s",
                           response.statusCode(), response.body());
                throw new RuntimeException(
                        String.format("Unexpected error in Inventory service. Code %s, Message %s",
                                      response.statusCode(),
                                      response.body()));
        }

    }
}

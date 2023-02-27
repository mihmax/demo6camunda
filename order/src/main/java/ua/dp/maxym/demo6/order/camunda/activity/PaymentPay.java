package ua.dp.maxym.demo6.order.camunda.activity;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ua.dp.maxym.demo6.order.camunda.MainOrderService;
import ua.dp.maxym.demo6.order.domain.LogRepository;

import javax.validation.constraints.NotNull;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.SECONDS;

@Component
public class PaymentPay implements JavaDelegate {

    @Autowired
    private LogRepository logger;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        logger.log("PaymentPay called with %s", execution.getVariables());
        Double pricePerItem = (Double) execution.getVariable(MainOrderService.VARIABLE_PRICE_PER_ITEM);
        String user = (String) execution.getVariable(MainOrderService.VARIABLE_USER);
        @NotNull Integer quantity = (Integer) execution.getVariable(MainOrderService.VARIABLE_QUANTITY);
        double amount = quantity * pricePerItem;
        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(new URI("http://localhost:8083/pay"))
                                         .POST(HttpRequest.BodyPublishers.ofString(
                                                 String.format("user=%s&amount=%s", user, amount)))
                                         .header("Content-Type", "application/x-www-form-urlencoded")
                                         .timeout(Duration.of(10, SECONDS))
                                         .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        switch (response.statusCode()) {
            case HttpURLConnection.HTTP_BAD_REQUEST,
                    HttpURLConnection.HTTP_NOT_FOUND,
                    HttpURLConnection.HTTP_FORBIDDEN:
                // Expected (business) errors
                logger.log("ERROR! PaymentPay received expected error code %s, message %s",
                           response.statusCode(), response.body());
                execution.setVariable(MainOrderService.FAILURE_REASON, response.body());
                throw new BpmnError(MainOrderService.DO_NOT_RETRY,
                                    String.format("Business Validation error in Payment service %s", response.body()));
            case HttpURLConnection.HTTP_OK:
                // Success, getting UUID
                var idString = response.body();
                execution.setVariable(MainOrderService.VARIABLE_PAYMENT_ID, UUID.fromString(idString));
                logger.log("PaymentPay got good response from payment service, saving payment id %s", idString);
                break;
            default:
                // Unexpected error
                logger.log("ERROR! PaymentPay received unexpected error code %s, message %s",
                           response.statusCode(), response.body());
                throw new RuntimeException(
                        String.format("Unexpected error in Payment service. Code %s, Message %s",
                                      response.statusCode(), response.body()));
        }
    }
}

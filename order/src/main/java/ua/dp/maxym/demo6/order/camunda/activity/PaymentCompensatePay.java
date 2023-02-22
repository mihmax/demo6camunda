package ua.dp.maxym.demo6.order.camunda.activity;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ua.dp.maxym.demo6.order.camunda.MainOrderService;
import ua.dp.maxym.demo6.order.domain.LogRepository;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.SECONDS;

@Component
public class PaymentCompensatePay implements JavaDelegate {

    @Autowired
    private LogRepository logger;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        logger.log("PaymentCompensatePay called with %s", execution.getVariables());
        UUID paymentId = (UUID) execution.getVariable(MainOrderService.VARIABLE_PAYMENT_ID);
        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(new URI(String.format("http://localhost:8083/cancelPayment?paymentId=%s",
                                                                    paymentId)))
                                         .DELETE()
                                         .timeout(Duration.of(10, SECONDS))
                                         .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        switch (response.statusCode()) {
            case HttpURLConnection.HTTP_BAD_REQUEST,
                    HttpURLConnection.HTTP_NOT_FOUND:
                // Expected (business) errors
                throw new RuntimeException(
                        String.format("Business Validation error during payment cancellation %s",
                                      response.body()));
            case HttpURLConnection.HTTP_OK:
                // Success, nothing to do
                break;
            default:
                // Unexpected error
                throw new RuntimeException(
                        String.format("Unexpected error during payment cancellation. Code %s, Message %s",
                                      response.statusCode(),
                                      response.body()));
        }
    }
}

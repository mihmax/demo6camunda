package ua.dp.maxym.demo5.payment.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ua.dp.maxym.demo5.payment.domain.Payment;
import ua.dp.maxym.demo5.payment.domain.PaymentRepository;
import ua.dp.maxym.demo5.payment.domain.UserCredit;
import ua.dp.maxym.demo5.payment.domain.UserCreditRepository;

import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class PaymentController {

    @Autowired
    private UserCreditRepository userCreditRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @GetMapping({"/", "/list"})
    public String list() {
        return String.format("""
                                     List of user credit:
                                     <br/><br/>
                                     %1$s
                                     """,
                             userCreditRepository.findAll().stream().map(Object::toString)
                                                 .collect(Collectors.joining("<br/>")));
    }

    @PostMapping("/pay")
    public String pay(@RequestParam("user") String user, @RequestParam("amount") Double amount,
                      HttpServletResponse response) {
        if (user == null || user.isEmpty() || amount == null || amount.isNaN() || amount == 0.0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return String.format("Error, invalid parameters. Expecting user (got %s) and amount (got %s).",
                                 user, amount);
        }

        UserCredit credit = userCreditRepository.findByUser(user);
        if (credit == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return String.format("Error, user %s not found", user);
        }
        if (credit.credit() < amount) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return String.format("Error, user %s does not have enough funds (requested %s but got %s)",
                                 user, amount, credit.credit());
        }

        var paymentId = UUID.randomUUID();
        var payment = new Payment(paymentId, user, amount);
        paymentRepository.insert(payment);
        var newCredit = new UserCredit(user, credit.credit() - amount);
        userCreditRepository.save(newCredit);
        return paymentId.toString();
    }

    private <T1, T2> T1 tryOrNull(Function1<T1, T2> function, T2 arg1) {
        try {
            return function.apply(arg1);
        } catch (Exception e) {
            return null;
        }
    }

    @DeleteMapping("/cancelPayment")
    public String cancelPayment(String paymentId, HttpServletResponse response) {
        if (paymentId == null || paymentId.isEmpty() || tryOrNull(UUID::fromString, paymentId) == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return String.format("Error, invalid parameters. Expecting payment id in UUID format (got %s).",
                                 paymentId);
        }

        UUID id = UUID.fromString(paymentId);
        var payment = paymentRepository.findById(id);
        if (payment.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return String.format("Error, payment %s not found", paymentId);
        }

        var credit = userCreditRepository.findByUser(payment.get().user());
        var newCredit = new UserCredit(credit.user(), credit.credit() + payment.get().amount());
        userCreditRepository.save(newCredit);
        paymentRepository.delete(payment.get());

        return "OK. Payment deleted";
    }

    @FunctionalInterface
    private interface Function1<T1, T2> {
        T1 apply(T2 t1);
    }

}

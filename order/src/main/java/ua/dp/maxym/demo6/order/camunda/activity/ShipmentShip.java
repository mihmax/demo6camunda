package ua.dp.maxym.demo6.order.camunda.activity;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.context.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ua.dp.maxym.demo6.order.camunda.MainOrderService;
import ua.dp.maxym.demo6.order.domain.LogRepository;

@Component
public class ShipmentShip implements JavaDelegate {

    @Autowired
    private LogRepository logger;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        logger.log("ShipmentShip called with %s", execution.getVariables());
        logger.log("Triggering UnsupportedOperationException");
        if (Context.getJobExecutorContext().getCurrentJob().getRetries()<=1) {
            throw new BpmnError(MainOrderService.DO_NOT_RETRY);
        }
        throw new UnsupportedOperationException("Shipment not implemented");
    }
}

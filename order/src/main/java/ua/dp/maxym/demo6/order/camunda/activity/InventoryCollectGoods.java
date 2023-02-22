package ua.dp.maxym.demo6.order.camunda.activity;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ua.dp.maxym.demo6.order.domain.LogRepository;

@Component
public class InventoryCollectGoods implements JavaDelegate {

    @Autowired
    private LogRepository logger;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        // do nothing
        logger.log("InventoryCollectGoods called with %s", execution.getVariables());
    }
}

package ua.dp.maxym.demo6.order.camunda;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.dp.maxym.demo6.order.camunda.activity.*;
import ua.dp.maxym.demo6.order.domain.LogRepository;
import ua.dp.maxym.demo6.order.domain.Order;
import ua.dp.maxym.demo6.order.domain.OrderRepository;
import ua.dp.maxym.demo6.order.domain.OrderStatus;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.UUID;

@Service
public class MainOrderService implements ExecutionListener {

    public static final String VARIABLE_ORDER_ID = "orderId";
    public static final String VARIABLE_USER = "user";
    public static final String VARIABLE_GOODS = "goods";
    public static final String VARIABLE_QUANTITY = "quantity";
    public static final String VARIABLE_PRICE_PER_ITEM = "pricePerItem";
    public static final String VARIABLE_PAYMENT_ID = "paymentId";
    private static final String ORDER_WORKFLOW_NAME = "OrderWorkflow";
    public static final String DO_NOT_RETRY = "DoNotRetry";
    @Autowired
    private ProcessEngine camunda;

    @Autowired
    private LogRepository logger;

    @Autowired
    private OrderRepository orderRepository;

    @PostConstruct
    void defineCamundaOrderModel() {
        BpmnModelInstance camundaOrderModel =
                ModelBuilderHelper.newModel(ORDER_WORKFLOW_NAME)
                                  .start()
                                  .activity("Get goods price from inventory", InventoryGetPrice.class)
                                  .parallelStart()
                                  .activity("Collect from inventory", InventoryCollectGoods.class)
                                  .compensationActivity("Compensate inventory collection",
                                                        InventoryCompensateCollectGoods.class)
                                  .parallelNext()
                                  .activity("Pay", PaymentPay.class)
                                  .compensationActivity("Compensate payment", PaymentCompensatePay.class)
                                  .parallelEnd()
                                  .activity("Ship", ShipmentShip.class)
                                  .end()
                                  .addListener(ExecutionListener.EVENTNAME_START, this.getClass())
                                  .triggerCompensationOnError(DO_NOT_RETRY)
                                  .addListener(ExecutionListener.EVENTNAME_START, this.getClass())
                                  .build();
        camunda.getRepositoryService().createDeployment()
               .addModelInstance("order.bpmn", camundaOrderModel)
               .deploy();
    }

    public UUID order(String user, String goods, Integer quantity) {
        Order order = new Order(UUID.randomUUID(), OrderStatus.NOT_STARTED, null, user, goods, quantity, null);
        orderRepository.save(order);
        camunda.getProcessEngineConfiguration().setDefaultNumberOfRetries(10);

        ProcessInstance process =
                camunda.getRuntimeService()
                       .startProcessInstanceByKey(ORDER_WORKFLOW_NAME,
                                                  Map.of(VARIABLE_ORDER_ID, order.id(),
                                                         VARIABLE_USER, user,
                                                         VARIABLE_GOODS, goods,
                                                         VARIABLE_QUANTITY, quantity));
        orderRepository.updateOrderStatus(order.id(), OrderStatus.INITIATED);
        return order.id();
    }

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        logger.log("Notification listener is called that job is complete %s", execution);
        UUID orderId = (UUID) execution.getVariable(VARIABLE_ORDER_ID);
        if (execution.getCurrentActivityName().contains(DO_NOT_RETRY)) {
            // Ending via compensation flow due to error
            orderRepository.updateOrderStatus(orderId, OrderStatus.FAILED);
        } else {
            orderRepository.updateOrderStatus(orderId, OrderStatus.SUCCEEDED);
        }
    }
}

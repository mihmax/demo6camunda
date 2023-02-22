package ua.dp.maxym.demo6.order.camunda;

import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.AbstractActivityBuilder;
import org.camunda.bpm.model.bpmn.builder.AbstractFlowNodeBuilder;
import org.camunda.bpm.model.bpmn.builder.ProcessBuilder;

import java.util.Stack;

public class ModelBuilderHelper {

    private final String name;
    private final Stack<ParallelGatewayData> parallelGateways = new Stack<>();
    private int parallelGatewayNumber = 0;
    @SuppressWarnings("rawtypes")
    private AbstractFlowNodeBuilder saga;
    private BpmnModelInstance bpmnModelInstance;
    private ProcessBuilder process;
    private String retryTimeCycle;

    public ModelBuilderHelper(String name) {
        this(name, "R4/PT9S");
    }
    public ModelBuilderHelper(String name, String retryTimeCycle) {
        this.name = name;
        this.retryTimeCycle = retryTimeCycle;
        process = Bpmn.createExecutableProcess(name);
    }

    public static ModelBuilderHelper newModel(String name) {
        return new ModelBuilderHelper(name);
    }

    public BpmnModelInstance build() {
        if (bpmnModelInstance == null) {
            bpmnModelInstance = saga.done();
        }
        return bpmnModelInstance;
    }

    public ModelBuilderHelper start() {
        saga = process.startEvent("Start-" + name);
        return this;
    }

    public ModelBuilderHelper end() {
        saga = saga.endEvent("EndSuccess-" + name);
        return this;
    }

    @SuppressWarnings("rawtypes")
    public ModelBuilderHelper activity(String name, Class adapterClass) {
        // this is very handy and could also be done inline above directly
        String id = "Activity-" + safeForId(name); // risky thing ;-)
        saga = saga.serviceTask(id).name(name).camundaClass(adapterClass)
                   .camundaAsyncBefore()
                   .camundaFailedJobRetryTimeCycle(retryTimeCycle);
        return this;
    }

    @SuppressWarnings("rawtypes")
    public ModelBuilderHelper compensationActivity(String name, Class adapterClass) {
        if (!(saga instanceof AbstractActivityBuilder)) {
            throw new RuntimeException("Compensation activity can only be specified right after activity");
        }

        String id = "Activity-" + safeForId(name) + "-compensation"; // risky thing ;-)

        ((AbstractActivityBuilder) saga)
                .boundaryEvent()
                .compensateEventDefinition()
                .compensateEventDefinitionDone()
                .compensationStart()
                .serviceTask(id).name(name).camundaClass(adapterClass)
                .camundaAsyncBefore()
                .camundaFailedJobRetryTimeCycle(retryTimeCycle)
                .compensationDone();

        return this;
    }

    public ModelBuilderHelper triggerCompensationOnError(String errorCode) {
        saga = process.eventSubProcess()
               .startEvent("CatchError-" + safeForId(errorCode))
//               .error("java.lang.Throwable")
               .error(errorCode)
               .intermediateThrowEvent("Compensate-"+safeForId(errorCode))
                      .compensateEventDefinition().compensateEventDefinitionDone()
               .endEvent("EndError-"+safeForId(errorCode));

        return this;
    }

    /**
     * Allows to register listener to events and activities. Call it immediately after building event or activity.
     *
     * @param event         One of {@link ExecutionListener#EVENTNAME_START}, {@link ExecutionListener#EVENTNAME_END},
     *                      {@link ExecutionListener#EVENTNAME_TAKE}
     * @param listenerClass Class implementing {@link ExecutionListener} interface that will be notified
     * @return builder
     */
    public ModelBuilderHelper addListener(String event, Class<? extends ExecutionListener> listenerClass) {
        saga = saga.camundaExecutionListenerClass(event, listenerClass);
        return this;
    }

    public ModelBuilderHelper parallelStart() {
        parallelGatewayNumber++;
        int depth = parallelGateways.size();
        String id = parallelGatewayNumber + "-" + depth;
        var gateway = new ParallelGatewayData(id, false);
        parallelGateways.push(gateway);

        saga = saga.parallelGateway(gateway.forkId());

        return this;
    }

    public ModelBuilderHelper parallelNext() {
        if (parallelGateways.isEmpty()) {
            throw new RuntimeException(
                    "Error. You are trying to start parallel thread but no parallel gateway has been started before!");
        }
        ParallelGatewayData gateway = parallelGateways.peek();
        if (!gateway.hasParallelTasks) {
            // first time, need to create join gateway
            saga = saga.parallelGateway(gateway.joinId())
                       // and move to the fork
                       .moveToNode(gateway.forkId());
            // and raise the flag
            gateway.hasParallelTasks = true;
        } else {
            // 2nd or more-th time, need to connect to proper join gateway
            saga = saga.connectTo(gateway.joinId())
                       // and move to the fork again
                       .moveToNode(gateway.forkId());
        }

        return this;
    }

    public ModelBuilderHelper parallelEnd() {
        if (parallelGateways.isEmpty()) {
            throw new RuntimeException(
                    "Error. You are trying to end parallel gateway while it has not started before!");
        }
        ParallelGatewayData gateway = parallelGateways.pop();
        if (!gateway.hasParallelTasks) {
            throw new RuntimeException(
                    "Error. This parallel gateway did not have any parallel tasks! Such configuration is not " +
                            "supported");
        }
        saga = saga.connectTo(gateway.joinId());

        return this;
    }

    private String safeForId(String name) {
        return name.replace(" ", "-");
    }

    private class ParallelGatewayData {
        private final String id;
        private boolean hasParallelTasks;

        public ParallelGatewayData(String id, boolean hasParallelTasks) {
            this.id = id;
            this.hasParallelTasks = hasParallelTasks;
        }

        private String forkId() {
            return "fork-" + id;
        }

        private String joinId() {
            return "join-" + id;
        }

    }
}
